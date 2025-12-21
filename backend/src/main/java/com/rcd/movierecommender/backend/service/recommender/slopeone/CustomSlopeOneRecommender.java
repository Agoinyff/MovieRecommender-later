package com.rcd.movierecommender.backend.service.recommender.slopeone;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.common.Refreshable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A lightweight Slope One recommender that depends only on Mahout Taste {@link DataModel}/{@link org.apache.mahout.cf.taste.recommender.Recommender}.
 *
 * <p>This implementation pre-computes item-to-item deviations and frequencies from the {@link DataModel}
 * and uses them to estimate scores for unrated items. It intentionally avoids {@code mahout-integration}
 * modules that have been removed from newer releases.</p>
 */
public class CustomSlopeOneRecommender extends AbstractRecommender {

    private final DataModel dataModel;
    private final Map<Long, Map<Long, DeviationStat>> deviations = new HashMap<>();

    public CustomSlopeOneRecommender(DataModel dataModel) throws TasteException {
        super(dataModel);
        this.dataModel = dataModel;
        buildDeviations();
    }

    @Override
    public List<RecommendedItem> recommend(long userID, int howMany) throws TasteException {
        return recommend(userID, howMany, null);
    }

    @Override
    public List<RecommendedItem> recommend(long userID, int howMany, IDRescorer rescorer) throws TasteException {
        PreferenceArray userPreferences = dataModel.getPreferencesFromUser(userID);
        Set<Long> ratedItems = new HashSet<>();
        for (int i = 0; i < userPreferences.length(); i++) {
            ratedItems.add(userPreferences.getItemID(i));
        }

        List<RecommendedItem> candidates = new ArrayList<>();
        LongPrimitiveIterator itemIterator = dataModel.getItemIDs();
        while (itemIterator.hasNext()) {
            long itemId = itemIterator.nextLong();
            if (ratedItems.contains(itemId)) {
                continue;
            }
            double estimate = estimateFromUserPreferences(userPreferences, itemId);
            if (Double.isNaN(estimate)) {
                continue;
            }
            float finalScore = (float) estimate;
            if (rescorer != null) {
                if (rescorer.isFiltered(itemId)) {
                    continue;
                }
                finalScore = (float) rescorer.rescore(itemId, finalScore);
            }
            candidates.add(new GenericRecommendedItem(itemId, finalScore));
        }

        return candidates.stream()
                .sorted(Comparator.comparing(RecommendedItem::getValue).reversed())
                .limit(howMany)
                .collect(Collectors.toList());
    }

    @Override
    public float estimatePreference(long userID, long itemID) throws TasteException {
        PreferenceArray userPreferences = dataModel.getPreferencesFromUser(userID);
        double estimate = estimateFromUserPreferences(userPreferences, itemID);
        if (Double.isNaN(estimate)) {
            throw new TasteException("Not enough data to estimate preference for item " + itemID);
        }
        return (float) estimate;
    }

    @Override
    public void setPreference(long userID, long itemID, float value) {
        throw new UnsupportedOperationException("setPreference is not supported in CustomSlopeOneRecommender");
    }

    @Override
    public void removePreference(long userID, long itemID) {
        throw new UnsupportedOperationException("removePreference is not supported in CustomSlopeOneRecommender");
    }

    @Override
    public DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public void refresh(Collection<Refreshable> alreadyRefreshed) {
        // Stateless after construction; no-op refresh.
    }

    private void buildDeviations() throws TasteException {
        LongPrimitiveIterator userIterator = dataModel.getUserIDs();
        while (userIterator.hasNext()) {
            long userId = userIterator.nextLong();
            PreferenceArray prefs = dataModel.getPreferencesFromUser(userId);
            int size = prefs.length();
            for (int i = 0; i < size; i++) {
                long itemI = prefs.getItemID(i);
                double valueI = prefs.getValue(i);
                for (int j = i + 1; j < size; j++) {
                    long itemJ = prefs.getItemID(j);
                    double valueJ = prefs.getValue(j);
                    addDeviation(itemI, itemJ, valueI - valueJ);
                    addDeviation(itemJ, itemI, valueJ - valueI);
                }
            }
        }
    }

    private void addDeviation(long itemA, long itemB, double difference) {
        Map<Long, DeviationStat> itemDeviation = deviations.computeIfAbsent(itemA, key -> new HashMap<>());
        itemDeviation.computeIfAbsent(itemB, key -> new DeviationStat()).add(difference);
    }

    private double estimateFromUserPreferences(PreferenceArray userPreferences, long targetItemId) {
        double weightedSum = 0.0;
        int totalCount = 0;

        for (int i = 0; i < userPreferences.length(); i++) {
            long ratedItemId = userPreferences.getItemID(i);
            if (ratedItemId == targetItemId) {
                return Double.NaN;
            }

            DeviationStat stat = getDeviation(targetItemId, ratedItemId);
            if (stat == null || stat.getCount() == 0) {
                continue;
            }

            weightedSum += (stat.getAverage() + userPreferences.getValue(i)) * stat.getCount();
            totalCount += stat.getCount();
        }

        if (totalCount == 0) {
            return Double.NaN;
        }
        return weightedSum / totalCount;
    }

    private DeviationStat getDeviation(long itemA, long itemB) {
        Map<Long, DeviationStat> deviationsForItem = deviations.get(itemA);
        if (deviationsForItem == null) {
            return null;
        }
        return deviationsForItem.get(itemB);
    }

    private static class DeviationStat {
        private double total = 0.0;
        private int count = 0;

        void add(double delta) {
            total += delta;
            count++;
        }

        double getAverage() {
            return count == 0 ? Double.NaN : total / count;
        }

        int getCount() {
            return count;
        }
    }
}
