/*
 * The MIT License
 *
 * Copyright (c) 2014 The Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package htsjdk.samtools.util;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.variant.vcf.VCFFileReader;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Tests the IntervalList class
 */
public class NamedIntervalListTest {

    final SAMFileHeader fileHeader;

    final NamedIntervalList list1, list2, list3;

    public NamedIntervalListTest() {
        fileHeader = NamedIntervalList.fromFile(new File("testdata/htsjdk/samtools/intervallist/IntervalListchr123_empty.interval_list")).getHeader();
        fileHeader.setSortOrder(SAMFileHeader.SortOrder.unsorted);

        list1 = new NamedIntervalList(fileHeader);
        list2 = new NamedIntervalList(fileHeader);
        list3 = new NamedIntervalList(fileHeader);


        list1.add(new NamedInterval("1", 1, 100));     //de-facto: 1:1-200 1:202-300     2:100-150 2:200-300
        list1.add(new NamedInterval("1", 101, 200));
        list1.add(new NamedInterval("1", 202, 300));
        list1.add(new NamedInterval("2", 200, 300));
        list1.add(new NamedInterval("2", 100, 150));

        list2.add(new NamedInterval("1", 50, 150));   //de-facto 1:50-150 1:301-500      2:1-150 2:250-270 2:290-400
        list2.add(new NamedInterval("1", 301, 500));
        list2.add(new NamedInterval("2", 1, 150));
        list2.add(new NamedInterval("2", 250, 270));
        list2.add(new NamedInterval("2", 290, 400));

        list3.add(new NamedInterval("1", 25, 400));    //de-facto 1:25-400                2:200-600                            3:50-470
        list3.add(new NamedInterval("2", 200, 600));
        list3.add(new NamedInterval("3", 50, 470));
    }


    @DataProvider(name = "intersectData")
    public Object[][] intersectData() {
        final NamedIntervalList intersect123 = new NamedIntervalList(fileHeader);
        final NamedIntervalList intersect12 = new NamedIntervalList(fileHeader);
        final NamedIntervalList intersect13 = new NamedIntervalList(fileHeader);
        final NamedIntervalList intersect23 = new NamedIntervalList(fileHeader);

        intersect123.add(new NamedInterval("1", 50, 150));
        intersect123.add(new NamedInterval("2", 250, 270));
        intersect123.add(new NamedInterval("2", 290, 300));

        intersect12.add(new NamedInterval("1", 50, 150));
        intersect12.add(new NamedInterval("2", 100, 150));
        intersect12.add(new NamedInterval("2", 250, 270));
        intersect12.add(new NamedInterval("2", 290, 300));

        intersect13.add(new NamedInterval("1", 25, 200));
        intersect13.add(new NamedInterval("1", 202, 300));
        intersect13.add(new NamedInterval("2", 200, 300));

        intersect23.add(new NamedInterval("1", 50, 150));
        intersect23.add(new NamedInterval("1", 301, 400));
        intersect23.add(new NamedInterval("2", 250, 270));
        intersect23.add(new NamedInterval("2", 290, 400));


        return new Object[][]{
                new Object[]{Arrays.asList(list1, list2, list3), intersect123},
                new Object[]{Arrays.asList(list1, list2), intersect12},
                new Object[]{Arrays.asList(list2, list1), intersect12},
                new Object[]{Arrays.asList(list2, list3), intersect23},
                new Object[]{Arrays.asList(list3, list2), intersect23},
                new Object[]{Arrays.asList(list1, list3), intersect13},
                new Object[]{Arrays.asList(list3, list1), intersect13}
        };
    }

    @Test(dataProvider = "intersectData")
    public void testIntersectIntervalLists(final List<NamedIntervalList> lists, final NamedIntervalList list) {
        Assert.assertEquals(
                CollectionUtil.makeCollection(NamedIntervalList.intersection(lists).iterator()),
                CollectionUtil.makeCollection(list.iterator()));
    }

    @DataProvider(name = "mergeData")
    public Object[][] mergeData() {
        final NamedIntervalList merge123 = new NamedIntervalList(fileHeader);
        final NamedIntervalList merge12 = new NamedIntervalList(fileHeader);
        final NamedIntervalList merge23 = new NamedIntervalList(fileHeader);
        final NamedIntervalList merge13 = new NamedIntervalList(fileHeader);


        merge123.add(new NamedInterval("1", 1, 100));     //de-facto: 1:1-200 1:202-300     2:100-150 2:200-300
        merge123.add(new NamedInterval("1", 101, 200));
        merge123.add(new NamedInterval("1", 202, 300));
        merge123.add(new NamedInterval("2", 200, 300));
        merge123.add(new NamedInterval("2", 100, 150));

        merge123.add(new NamedInterval("1", 50, 150));   //de-facto 1:50-150 1:301-500      2:1-150 2:250-270 2:290-400
        merge123.add(new NamedInterval("1", 301, 500));
        merge123.add(new NamedInterval("2", 1, 150));
        merge123.add(new NamedInterval("2", 250, 270));
        merge123.add(new NamedInterval("2", 290, 400));

        merge123.add(new NamedInterval("1", 25, 400));    //de-facto 1:25-400                2:200-600                            3:50-470
        merge123.add(new NamedInterval("2", 200, 600));
        merge123.add(new NamedInterval("3", 50, 470));


        merge12.add(new NamedInterval("1", 1, 100));     //de-facto: 1:1-200 1:202-300     2:100-150 2:200-300
        merge12.add(new NamedInterval("1", 101, 200));
        merge12.add(new NamedInterval("1", 202, 300));
        merge12.add(new NamedInterval("2", 200, 300));
        merge12.add(new NamedInterval("2", 100, 150));

        merge12.add(new NamedInterval("1", 50, 150));   //de-facto 1:50-150 1:301-500      2:1-150 2:250-270 2:290-400
        merge12.add(new NamedInterval("1", 301, 500));
        merge12.add(new NamedInterval("2", 1, 150));
        merge12.add(new NamedInterval("2", 250, 270));
        merge12.add(new NamedInterval("2", 290, 400));

        merge23.add(new NamedInterval("1", 50, 150));   //de-facto 1:50-150 1:301-500      2:1-150 2:250-270 2:290-400
        merge23.add(new NamedInterval("1", 301, 500));
        merge23.add(new NamedInterval("2", 1, 150));
        merge23.add(new NamedInterval("2", 250, 270));
        merge23.add(new NamedInterval("2", 290, 400));

        merge23.add(new NamedInterval("1", 25, 400));    //de-facto 1:25-400                2:200-600                            3:50-470
        merge23.add(new NamedInterval("2", 200, 600));
        merge23.add(new NamedInterval("3", 50, 470));


        merge13.add(new NamedInterval("1", 1, 100));     //de-facto: 1:1-200 1:202-300     2:100-150 2:200-300
        merge13.add(new NamedInterval("1", 101, 200));
        merge13.add(new NamedInterval("1", 202, 300));
        merge13.add(new NamedInterval("2", 200, 300));
        merge13.add(new NamedInterval("2", 100, 150));

        merge13.add(new NamedInterval("1", 25, 400));    //de-facto 1:25-400                2:200-600                            3:50-470
        merge13.add(new NamedInterval("2", 200, 600));
        merge13.add(new NamedInterval("3", 50, 470));


        return new Object[][]{
                new Object[]{Arrays.asList(list1, list2, list3), merge123},
                new Object[]{Arrays.asList(list1, list2), merge12},
                new Object[]{Arrays.asList(list2, list3), merge23},
                new Object[]{Arrays.asList(list1, list3), merge13}

        };
    }


    @Test(dataProvider = "mergeData")
    public void testMergeIntervalLists(final List<NamedIntervalList> lists, final NamedIntervalList list) {
        Assert.assertEquals(
                CollectionUtil.makeCollection(NamedIntervalList.concatenate(lists).iterator()),
                CollectionUtil.makeCollection(list.iterator()));
    }


    @DataProvider(name = "unionData")
    public Object[][] unionData() {
        final NamedIntervalList union123 = new NamedIntervalList(fileHeader);
        final NamedIntervalList union12 = new NamedIntervalList(fileHeader);
        final NamedIntervalList union13 = new NamedIntervalList(fileHeader);
        final NamedIntervalList union23 = new NamedIntervalList(fileHeader);

        union123.add(new NamedInterval("1", 1, 500));
        union123.add(new NamedInterval("2", 1, 150));
        union123.add(new NamedInterval("2", 200, 600));
        union123.add(new NamedInterval("3", 50, 470));

        union12.add(new NamedInterval("1", 1, 200));
        union12.add(new NamedInterval("1", 202, 500));
        union12.add(new NamedInterval("2", 1, 150));
        union12.add(new NamedInterval("2", 200, 400));


        union23.add(new NamedInterval("1", 25, 500));
        union23.add(new NamedInterval("2", 1, 150));
        union23.add(new NamedInterval("2", 200, 600));
        union23.add(new NamedInterval("3", 50, 470));

        union13.add(new NamedInterval("1", 1, 400));
        union13.add(new NamedInterval("2", 100, 150));
        union13.add(new NamedInterval("2", 200, 600));
        union13.add(new NamedInterval("3", 50, 470));


        return new Object[][]{
                new Object[]{Arrays.asList(list1, list2, list3), union123},
                new Object[]{Arrays.asList(list1, list2), union12},
                new Object[]{Arrays.asList(list1, list2), union12},
                new Object[]{Arrays.asList(list2, list3), union23},
                new Object[]{Arrays.asList(list2, list3), union23},
                new Object[]{Arrays.asList(list1, list3), union13},
                new Object[]{Arrays.asList(list1, list3), union13}
        };
    }

    @Test(dataProvider = "unionData", enabled = true)
    public void testUnionIntervalLists(final List<NamedIntervalList> lists, final NamedIntervalList list) {
        Assert.assertEquals(
                CollectionUtil.makeCollection(NamedIntervalList.union(lists).iterator()),
                CollectionUtil.makeCollection(list.iterator()));
    }

    @DataProvider(name = "invertData")
    public Object[][] invertData() {
        final NamedIntervalList invert1 = new NamedIntervalList(fileHeader);
        final NamedIntervalList invert2 = new NamedIntervalList(fileHeader);
        final NamedIntervalList invert3 = new NamedIntervalList(fileHeader);

        final NamedIntervalList full = new NamedIntervalList(fileHeader);
        final NamedIntervalList fullChopped = new NamedIntervalList(fileHeader);
        final NamedIntervalList empty = new NamedIntervalList(fileHeader);


        invert1.add(new NamedInterval("1", 201, 201));
        invert1.add(new NamedInterval("1", 301, fileHeader.getSequence("1").getSequenceLength()));
        invert1.add(new NamedInterval("2", 1, 99));
        invert1.add(new NamedInterval("2", 151, 199));
        invert1.add(new NamedInterval("2", 301, fileHeader.getSequence("2").getSequenceLength()));
        invert1.add(new NamedInterval("3", 1, fileHeader.getSequence("3").getSequenceLength()));

        invert2.add(new NamedInterval("1", 1, 49));
        invert2.add(new NamedInterval("1", 151, 300));
        invert2.add(new NamedInterval("1", 501, fileHeader.getSequence("1").getSequenceLength()));
        invert2.add(new NamedInterval("2", 151, 249));
        invert2.add(new NamedInterval("2", 271, 289));
        invert2.add(new NamedInterval("2", 401, fileHeader.getSequence("2").getSequenceLength()));
        invert2.add(new NamedInterval("3", 1, fileHeader.getSequence("3").getSequenceLength()));

        invert3.add(new NamedInterval("1", 1, 24));
        invert3.add(new NamedInterval("1", 401, fileHeader.getSequence("1").getSequenceLength()));
        invert3.add(new NamedInterval("2", 1, 199));
        invert3.add(new NamedInterval("2", 601, fileHeader.getSequence("2").getSequenceLength()));
        invert3.add(new NamedInterval("3", 1, 49));
        invert3.add(new NamedInterval("3", 471, fileHeader.getSequence("3").getSequenceLength()));

        for (final SAMSequenceRecord samSequenceRecord : fileHeader.getSequenceDictionary().getSequences()) {
            full.add(new NamedInterval(samSequenceRecord.getSequenceName(), 1, samSequenceRecord.getSequenceLength()));

            fullChopped.add(new NamedInterval(samSequenceRecord.getSequenceName(), 1, samSequenceRecord.getSequenceLength() / 2));
            fullChopped.add(new NamedInterval(samSequenceRecord.getSequenceName(), samSequenceRecord.getSequenceLength() / 2 + 1, samSequenceRecord.getSequenceLength()));
        }


        return new Object[][]{
                new Object[]{list1, invert1},
                new Object[]{list2, invert2},
                new Object[]{list3, invert3},
                new Object[]{full, empty},
                new Object[]{empty, full},
                new Object[]{fullChopped, empty}
        };
    }


    @Test(dataProvider = "invertData")
    public void testInvertSquared(final NamedIntervalList list, @SuppressWarnings("UnusedParameters") final NamedIntervalList ignored) throws Exception {
        final NamedIntervalList inverseSquared = NamedIntervalList.invert(NamedIntervalList.invert(list));
        final NamedIntervalList originalClone = new NamedIntervalList(list.getHeader());

        for (final NamedInterval interval : list) {
            originalClone.add(interval);
        }

        Assert.assertEquals(
                CollectionUtil.makeCollection(inverseSquared.iterator()),
                CollectionUtil.makeCollection(originalClone.uniqued().iterator()));
    }

    @Test(dataProvider = "invertData")
    public void testInvert(final NamedIntervalList list, final NamedIntervalList inverse) throws Exception {
        Assert.assertEquals(
                CollectionUtil.makeCollection(NamedIntervalList.invert(list).iterator()),
                CollectionUtil.makeCollection(inverse.iterator()));
    }


    @DataProvider(name = "subtractSingletonData")
    public Object[][] subtractSingletonData() {
        final NamedIntervalList subtract1_from_2 = new NamedIntervalList(fileHeader);
        final NamedIntervalList subtract2_from_3 = new NamedIntervalList(fileHeader);
        final NamedIntervalList subtract1_from_3 = new NamedIntervalList(fileHeader);
        final NamedIntervalList subtract3_from_1 = new NamedIntervalList(fileHeader);



        subtract1_from_2.add(new NamedInterval("1", 301, 500));
        subtract1_from_2.add(new NamedInterval("2", 1, 99));
        subtract1_from_2.add(new NamedInterval("2", 301, 400));


        subtract2_from_3.add(new NamedInterval("1", 25, 49));
        subtract2_from_3.add(new NamedInterval("1", 151, 300));
        subtract2_from_3.add(new NamedInterval("2", 200, 249));
        subtract2_from_3.add(new NamedInterval("2", 271, 289));
        subtract2_from_3.add(new NamedInterval("2", 401, 600));
        subtract2_from_3.add(new NamedInterval("3", 50, 470));

        subtract1_from_3.add(new NamedInterval("1", 201, 201));
        subtract1_from_3.add(new NamedInterval("1", 301, 400));
        subtract1_from_3.add(new NamedInterval("2", 301, 600));
        subtract1_from_3.add(new NamedInterval("3", 50, 470));

        subtract3_from_1.add(new NamedInterval("1", 1, 49));    //de-facto 1:25-400                2:200-600                            3:50-470
        subtract3_from_1.add(new NamedInterval("2", 100, 150));


        return new Object[][]{
                new Object[]{list2, list1, subtract1_from_2},
                new Object[]{list3, list2, subtract2_from_3},
                new Object[]{list3, list1, subtract1_from_3},
        };
    }

    @DataProvider(name = "subtractData")
    public Object[][] subtractData() {
        final NamedIntervalList subtract12_from_3 = new NamedIntervalList(fileHeader);

        subtract12_from_3.add(new NamedInterval("1", 201, 201));
        subtract12_from_3.add(new NamedInterval("2", 401, 600));
        subtract12_from_3.add(new NamedInterval("3", 50, 470));


        return new Object[][]{
                new Object[]{CollectionUtil.makeList(list3), CollectionUtil.makeList(list1, list2), subtract12_from_3},
        };
    }


    @Test(dataProvider = "subtractData")
    public void testSubtractIntervalLists(final List<NamedIntervalList> fromLists, final List<NamedIntervalList> whatLists, final NamedIntervalList list) {
        Assert.assertEquals(
                CollectionUtil.makeCollection(NamedIntervalList.subtract(fromLists, whatLists).iterator()),
                CollectionUtil.makeCollection(list.iterator()));
    }

    @Test(dataProvider = "subtractSingletonData")
    public void testSubtractSingletonIntervalLists(final NamedIntervalList fromLists, final NamedIntervalList whatLists, final NamedIntervalList list) {
        Assert.assertEquals(
                CollectionUtil.makeCollection(NamedIntervalList.subtract(fromLists, whatLists).iterator()),
                CollectionUtil.makeCollection(list.iterator()));
    }



    @Test(dataProvider = "subtractSingletonData")
    public void testSubtractSingletonasListIntervalList(final NamedIntervalList fromLists, final NamedIntervalList whatLists, final NamedIntervalList list) {
        Assert.assertEquals(
                CollectionUtil.makeCollection(NamedIntervalList.subtract(Collections.singletonList(fromLists), Collections.singletonList(whatLists)).iterator()),
                CollectionUtil.makeCollection(list.iterator()));
    }

    @DataProvider(name = "VCFCompData")
    public Object[][] VCFCompData() {
        return new Object[][]{
                new Object[]{"testdata/htsjdk/samtools/intervallist/IntervalListFromVCFTest.vcf", "testdata/htsjdk/samtools/intervallist/IntervalListFromVCFTestComp.interval_list", false},
                new Object[]{"testdata/htsjdk/samtools/intervallist/IntervalListFromVCFTest.vcf", "testdata/htsjdk/samtools/intervallist/IntervalListFromVCFTestCompInverse.interval_list", true},
                new Object[]{"testdata/htsjdk/samtools/intervallist/IntervalListFromVCFTestManual.vcf", "testdata/htsjdk/samtools/intervallist/IntervalListFromVCFTestManualComp.interval_list", false},
                new Object[]{"testdata/htsjdk/samtools/intervallist/IntervalListFromVCFTestManual.vcf", "testdata/htsjdk/samtools/intervallist/IntervalListFromVCFTestCompInverseManual.interval_list", true}
        };
    }


    @Test(dataProvider = "VCFCompData")
    public void testFromVCF(final String vcf, final String compInterval, final boolean invertVCF) {

        final File vcfFile = new File(vcf);
        final File compIntervalFile = new File(compInterval);

        final NamedIntervalList compList = NamedIntervalList.fromFile(compIntervalFile);
        final NamedIntervalList list = invertVCF ? NamedIntervalList.invert(VCFFileReader.fromVcf(vcfFile)) : VCFFileReader.fromVcf(vcfFile);

        compList.getHeader().getSequenceDictionary().assertSameDictionary(list.getHeader().getSequenceDictionary());

        final Collection<NamedInterval> intervals = CollectionUtil.makeCollection(list.iterator());
        final Collection<NamedInterval> compIntervals = CollectionUtil.makeCollection(compList.iterator());

        //assert that the intervals correspond
        Assert.assertEquals(intervals, compIntervals);

        final List<String> intervalNames = new LinkedList<String>();
        final List<String> compIntervalNames = new LinkedList<String>();

        for (final NamedInterval interval : intervals) {
            intervalNames.add(interval.getName());
        }
        for (final NamedInterval interval : compIntervals) {
            compIntervalNames.add(interval.getName());
        }
        //assert that the names match
        Assert.assertEquals(intervalNames, compIntervalNames);

    }

    @DataProvider
    public Object[][] testFromSequenceData() {
        return new Object[][]{
                new Object[]{"testdata/htsjdk/samtools/intervallist/IntervalListFromVCFTestComp.interval_list", "1", 249250621},
                new Object[]{"testdata/htsjdk/samtools/intervallist/IntervalListFromVCFTestComp.interval_list", "2", 243199373},
                new Object[]{"testdata/htsjdk/samtools/intervallist/IntervalListFromVCFTestComp.interval_list", "3", 198022430},
        };
    }

    @Test(dataProvider = "testFromSequenceData")
    public void testFromSequenceName(final String intervalList, final String referenceName, final Integer length) {

        final NamedIntervalList intervals = NamedIntervalList.fromFile(new File(intervalList));
        final NamedIntervalList test = NamedIntervalList.fromName(intervals.getHeader(), referenceName);
        Assert.assertEquals(test.getIntervals(), CollectionUtil.makeList(new NamedInterval(referenceName, 1, length)));
    }

    @Test
    public void testMerges() {
        SortedSet<NamedInterval> intervals = new TreeSet<NamedInterval>() {{
            add(new NamedInterval("1", 500, 600, false, "foo"));
            add(new NamedInterval("1", 550, 650, false, "bar"));
            add(new NamedInterval("1", 625, 699, false, "splat"));
        }};

        Interval out = NamedIntervalList.merge(intervals, false);
        Assert.assertEquals(out.getStart(), 500);
        Assert.assertEquals(out.getEnd(), 699);

        intervals.add(new NamedInterval("1", 626, 629, false, "whee"));
        out = NamedIntervalList.merge(intervals, false);
        Assert.assertEquals(out.getStart(), 500);
        Assert.assertEquals(out.getEnd(), 699);
    }
}