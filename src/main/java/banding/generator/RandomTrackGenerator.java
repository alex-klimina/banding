package banding.generator;

import banding.entity.Interval;
import banding.entity.Track;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RandomTrackGenerator {

    public static Map<String, Track> generateChromosomeSetByReferenceLike(
            Map<String, Track> reference,
            Map<String, Track> query) {
        Map<String, Track> generatedChromosomeSet = new HashMap<>();

        for (Map.Entry<String, Track> entry: reference.entrySet()) {
            Track randomTrack = generateRandomTrackByReferenceLike(reference.get(entry.getKey()), query.get(entry.getKey()));
            generatedChromosomeSet.put(entry.getKey(), randomTrack);
        }
        return generatedChromosomeSet;
    }

    public static Track generateRandomTrackByReferenceLike(Track reference, Track query) {
        int startReference = reference.getTrackStart();
        int endReference = reference.getTrackEnd();
        Track generatedTrack = new Track();

        for (Interval interval: query.getIntervals()) {
            boolean intersection = true;
            Interval randomInterval = null;
            while (intersection) {
                randomInterval = generateRandomInterval(interval.getLength(), startReference, endReference);
                intersection = Track.areIntervalAndTrackIntersect(randomInterval, generatedTrack);
            }
            generatedTrack.addInterval(randomInterval);
        }

        return generatedTrack;
    }

    public static Track generateRandomTrackLike(int length, Track track) {
        Track generatedTrack = new Track();

        for (Interval i: track.getIntervals()) {
            boolean intersection = true;
            Interval randomInterval = null;
            while (intersection) {
                randomInterval = generateRandomInterval(i.getLength(), 0, length);
                intersection = Track.areIntervalAndTrackIntersect(randomInterval, generatedTrack);
            }
            generatedTrack.addInterval(randomInterval);
        }

        return generatedTrack;
    }

    static Interval generateRandomInterval(Interval interval, Track track) {
        return generateRandomInterval(interval, track.getTrackStart(), track.getTrackEnd());
    }

    static Interval generateRandomInterval(Interval interval, int from, int to) {
        return generateRandomInterval(interval.getLength(), from, to);
    }

    static Interval generateRandomInterval(int length, int from, int to) {
        if ( (from + length) > to ) {
            throw new RuntimeException("It's impossible create an interval with length " + length
                    + " in track from " + from + " to " + to);
        }
        // if there is only one possibility for interval with that length
        if ( (from + length) == to ) {
            return new Interval(from, to);
        }

        Random random = new Random();
        Interval interval = null;
        while (interval == null) {
            int start = from + random.nextInt(to);
            if ((start + length) <= to) {
                interval = new Interval(start, start + length);
            }
        }
        return interval;
    }
}
