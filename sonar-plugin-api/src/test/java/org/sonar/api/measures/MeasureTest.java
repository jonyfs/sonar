/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.api.measures;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

public class MeasureTest {

  @Test
  public void scaleValue() {
    assertThat(new Measure(CoreMetrics.COVERAGE, 80.666666).getValue(), is(80.7));
    assertThat(new Measure(CoreMetrics.COVERAGE, 80.666666, 2).getValue(), is(80.67));
  }

  @Test
  public void defaultPersistenceModeIsFull() {
    assertThat(new Measure(CoreMetrics.LINES, 32.0).getPersistenceMode(), is(PersistenceMode.FULL));
  }

  @Test
  public void persistenceModeIsDatabaseForBigDataMeasures() {
    Measure bigDataMeasure = new Measure(CoreMetrics.COVERAGE_LINE_HITS_DATA, "long data")
        .setPersistenceMode(PersistenceMode.DATABASE);
    assertThat(bigDataMeasure.getPersistenceMode(), is(PersistenceMode.DATABASE));
  }

  @Test
  public void measureWithLevelValue() {
    assertThat(new Measure(CoreMetrics.ALERT_STATUS, Metric.Level.ERROR).getData(), is("ERROR"));
    assertThat(new Measure(CoreMetrics.ALERT_STATUS, Metric.Level.ERROR).getDataAsLevel(), is(Metric.Level.ERROR));
    assertThat(new Measure(CoreMetrics.ALERT_STATUS).setData(Metric.Level.ERROR).getDataAsLevel(), is(Metric.Level.ERROR));
  }

  @Test
  public void measureWithIntegerValue() {
    assertThat(new Measure(CoreMetrics.LINES).setIntValue(3).getValue(), is(3.0));
    assertThat(new Measure(CoreMetrics.LINES).setIntValue(null).getValue(), nullValue());

    assertThat(new Measure(CoreMetrics.LINES).setIntValue(3).getIntValue(), is(3));
    assertThat(new Measure(CoreMetrics.LINES).setIntValue(null).getIntValue(), nullValue());

    assertThat(new Measure(CoreMetrics.LINES).setValue(3.6).getIntValue(), is(3));
  }

  @Test
  public void valuesAreRoundUp() {
    assertThat(new Measure(CoreMetrics.COVERAGE, 5.22222222).getValue(), is(5.2));
    assertThat(new Measure(CoreMetrics.COVERAGE, 5.7777777).getValue(), is(5.8));

    assertThat(new Measure(CoreMetrics.COVERAGE, 5.22222222, 3).getValue(), is(5.222));
    assertThat(new Measure(CoreMetrics.COVERAGE, 5.7777777, 3).getValue(), is(5.778));
  }

  @Test
  public void equalsAndHashCode() {
    assertEquals(new Measure(CoreMetrics.COVERAGE, 50.0), new Measure(CoreMetrics.COVERAGE, 50.0));
    assertEquals(new Measure(CoreMetrics.COVERAGE, 50.0).hashCode(), new Measure(CoreMetrics.COVERAGE, 50.0).hashCode());
  }

  @Test(expected = IllegalArgumentException.class)
  public void tooLongDataForNumericMetric() {
    new Measure(CoreMetrics.COVERAGE, StringUtils.repeat("x", Measure.MAX_TEXT_SIZE + 1));
  }

  @Test
  public void longDataForDataMetric() {
    new Measure(CoreMetrics.COVERAGE_LINE_HITS_DATA, StringUtils.repeat("x", Measure.MAX_TEXT_SIZE + 1));
  }

  @Test
  public void shouldGetAndSetVariations() {
    Measure measure = new Measure(CoreMetrics.LINES).setVariation1(1d).setVariation2(2d).setVariation3(3d);
    assertThat(measure.getVariation1(), is(1d));
    assertThat(measure.getVariation2(), is(2d));
    assertThat(measure.getVariation3(), is(3d));
  }

  @Test
  public void shouldSetVariationsWithIndex() {
    Measure measure = new Measure(CoreMetrics.LINES).setVariation(2, 3.3);
    assertThat(measure.getVariation1(), nullValue());
    assertThat(measure.getVariation2(), is(3.3));
    assertThat(measure.getVariation3(), nullValue());
  }

  @Test
  public void notEqualRuleMeasures() {
    Measure measure = new Measure(CoreMetrics.VIOLATIONS, 30.0);
    RuleMeasure ruleMeasure = new RuleMeasure(CoreMetrics.VIOLATIONS, new Rule("foo", "bar"), RulePriority.CRITICAL, 3);
    assertFalse(measure.equals(ruleMeasure));
    assertFalse(ruleMeasure.equals(measure));
  }

  @Test
  public void shouldUnsetData() {
    String data = "1=10;21=456";
    Measure measure = new Measure(CoreMetrics.CONDITIONS_BY_LINE).setData( data);
    assertThat(measure.hasData(), is(true));
    assertThat(measure.getData(), is(data));

    measure.unsetData();

    assertThat(measure.hasData(), is(false));
    assertThat(measure.getData(), nullValue());
  }
}
