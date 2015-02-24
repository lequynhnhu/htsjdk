package htsjdk.samtools.util;

public class Interval implements Comparable<Interval> {
    protected final String sequence;
    protected final int start;
    protected final int end;

    public Interval(final String contig, final int start, final int end) {
        this.end = end;
        this.start = start;
        this.sequence = contig;
    }

    /** Gets the name of the sequence, chromosome, or contig on which the interval resides. */
    public String getSequence() { return sequence; }

    /** Gets the 1-based start position of the interval on the sequence. */
    public int getStart() { return start; }

    /** Gets the 1-based closed-ended end position of the interval on the sequence. */
    public int getEnd() { return end; }

    /** Returns true if this interval overlaps the other interval, otherwise false. */
    public boolean intersects(final Interval other) {
        return  (this.getSequence().equals(other.getSequence()) &&
                 CoordMath.overlaps(this.start, this.end, other.start, other.end));
    }

    public int getIntersectionLength(final Interval other) {
        if (this.intersects(other)) {
            return (int)CoordMath.getOverlap(this.getStart(), this.getEnd(), other.getStart(), other.getEnd());
        }
        return 0;
    }

    /**
     * Sort based on sequence.compareTo, then start pos, then end pos
     * with null objects coming lexically last
     */
    public int compareTo(final Interval that) {
        if (that == null) return -1; // nulls last

        int result = this.sequence.compareTo(that.sequence);
        if (result == 0) {
            if (this.start == that.start) {
                result = this.end - that.end;
            }
            else {
                result = this.start - that.start;
            }
        }

        return result;
    }

    /** Equals method that agrees with {@link #compareTo(Interval)}. */
    public boolean equals(final Object other) {
        if (!(other instanceof Interval)) return false;
        else if (this == other) return true;
        else {
            Interval that = (Interval)other;
            return (this.compareTo(that) == 0);
        }
    }

    @Override
    public int hashCode() {
        int result = sequence.hashCode();
        result = 31 * result + start;
        result = 31 * result + end;
        return result;
    }
}
