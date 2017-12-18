/*
  The MIT License (MIT)

  Copyright (c) 2014-2017 Marc de Verdelhan & respective authors (see AUTHORS)

  Permission is hereby granted, free of charge, to any person obtaining a copy of
  this software and associated documentation files (the "Software"), to deal in
  the Software without restriction, including without limitation the rights to
  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  the Software, and to permit persons to whom the Software is furnished to do so,
  subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators.adx;

import static org.junit.Assert.assertEquals;
import static org.ta4j.core.TATestsUtils.assertIndicatorEquals;

import org.junit.Test;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.IndicatorTest;
import org.ta4j.core.TATestsUtils;
import org.ta4j.core.TimeSeries;

public class MinusDIIndicatorTest extends IndicatorTest {

    public MinusDIIndicatorTest() throws Exception {
        super((data, params) -> new MinusDIIndicator((TimeSeries) data, (int) params[0]),
                "ADX.xls",
                13);
    }

    @Test
    public void testAgainstExternalData() throws Exception {
        TimeSeries series = getSeries();
        Indicator<Decimal> actualIndicator;
        
        actualIndicator = testIndicator(series, 1);
        assertIndicatorEquals(getIndicator(1), actualIndicator);
        assertEquals(0.0, actualIndicator.getValue(actualIndicator.getTimeSeries().getEndIndex()).doubleValue(), TATestsUtils.TA_OFFSET);

        actualIndicator = testIndicator(series, 3);
        assertIndicatorEquals(getIndicator(3), actualIndicator);
        assertEquals(21.0711, actualIndicator.getValue(actualIndicator.getTimeSeries().getEndIndex()).doubleValue(), TATestsUtils.TA_OFFSET);

        actualIndicator = testIndicator(series, 13);
        assertIndicatorEquals(getIndicator(13), actualIndicator);
        assertEquals(20.9020, actualIndicator.getValue(actualIndicator.getTimeSeries().getEndIndex()).doubleValue(), TATestsUtils.TA_OFFSET);
    }

}
