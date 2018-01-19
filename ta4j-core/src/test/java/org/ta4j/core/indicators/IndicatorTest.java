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
package org.ta4j.core.indicators;

import org.ta4j.core.Indicator;
import org.ta4j.core.IndicatorFactory;
import org.ta4j.core.TimeSeries;

public class IndicatorTest<D> {

    private final IndicatorFactory<D> factory;

    /**
     * Constructor.
     * @param factory the indicator factory
     */
    public IndicatorTest(IndicatorFactory<D> factory) {
        this.factory = factory;
    }

    /**
     * Getter for the indicator factory.
     * @return IndicatorFactory the factory
     */
    public IndicatorFactory<D> getFactory() {
        return factory;
    }

    /**
     * Creates an indicator from data and parameters.
     * @param series the data for the indicator
     * @param indicator the indicator for the series
     * @param params a set of indicator parameters
     * @return Indicator<Decimal> the indicator
     * @throws Exception
     */
    public Indicator<D> getIndicator(TimeSeries series, Indicator<D> indicator, Object... params) throws Exception {
        return factory.getIndicator(series,indicator, params);
    }

}
