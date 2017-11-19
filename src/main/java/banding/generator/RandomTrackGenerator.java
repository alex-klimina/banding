package banding.generator;

import banding.entity.Interval;
import banding.entity.Track;

import java.util.Random;

public class RandomTrackGenerator {

    public static Track generateRandomTrack(Track track, int length) {
        Track randomTrack = new Track();
        for (Interval interval: track.getIntervals()) {
            generateRandomInterval(interval.getLength(), 0, length);
        }
        return null;
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
            int start = random.nextInt(to);
            if ((start + length) <= to) {
                interval = new Interval(start, start + length);
            }
        }
        return interval;
    }

    static Track generateRandomTrackLike(int length, Track track) {
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
}
