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
package org.sonar.api.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparisons.greaterThan;
import static org.hamcrest.number.OrderingComparisons.greaterThanOrEqualTo;
import static org.hamcrest.text.StringStartsWith.startsWith;
import static org.junit.Assert.*;

public class DateUtilsTest {

  @Test
  public void shouldParseDate() {
    Date date = DateUtils.parseDate("2010-05-18");
    assertThat(date.getDate(), is(18));
  }

  @Test(expected = SonarException.class)
  public void shouldNotParseDate() {
    DateUtils.parseDate("2010/05/18");
  }

  @Test
  public void shouldParseDateTime() {
    Date date = DateUtils.parseDateTime("2010-05-18T15:50:45+0100");
    assertThat(date.getMinutes(), is(50));
  }

  @Test(expected = SonarException.class)
  public void shouldNotParseDateTime() {
    DateUtils.parseDate("2010/05/18 10:55");
  }

  @Test
  public void shouldFormatDate() {
    assertThat(DateUtils.formatDate(new Date()), startsWith("20"));
    assertThat(DateUtils.formatDate(new Date()).length(), is(10));
  }

  @Test
  public void shouldFormatDateTime() {
    assertThat(DateUtils.formatDateTime(new Date()), startsWith("20"));
    assertThat(DateUtils.formatDateTime(new Date()).length(), greaterThan(20));
  }

  /**
   * Cordially copied from XStream unit test
   * See http://koders.com/java/fid8A231D75F2C6E6909FB26BCA11C12D08AD05FB50.aspx?s=ThreadSafeDateFormatTest
   */
  @Test
  public void shouldBeThreadSafe() throws InterruptedException {

    final DateUtils.ThreadSafeDateFormat format = new DateUtils.ThreadSafeDateFormat("yyyy-MM-dd'T'HH:mm:ss,S z");
    final Date now = new Date();
    final List<Throwable> throwables = Lists.newArrayList();

    final ThreadGroup tg = new ThreadGroup("shouldBeThreadSafe") {
      public void uncaughtException(Thread t, Throwable e) {
        throwables.add(e);
        super.uncaughtException(t, e);
      }
    };

    final int[] counter = new int[1];
    counter[0] = 0;
    final Thread[] threads = new Thread[10];
    for (int i = 0; i < threads.length; ++i) {
      threads[i] = new Thread(tg, "JUnit Thread " + i) {

        public void run() {
          int i = 0;
          try {
            synchronized (this) {
              notifyAll();
              wait();
            }
            while (i < 1000 && !interrupted()) {
              String formatted = format.format(now);
              Thread.yield();
              assertEquals(now, format.parse(formatted));
              ++i;
            }
          } catch (Exception e) {
            fail("Unexpected exception: " + e);
          }
          synchronized (counter) {
            counter[0] += i;
          }
        }

      };
    }

    for (int i = 0; i < threads.length; ++i) {
      synchronized (threads[i]) {
        threads[i].start();
        threads[i].wait();
      }
    }

    for (int i = 0; i < threads.length; ++i) {
      synchronized (threads[i]) {
        threads[i].notifyAll();
      }
    }

    Thread.sleep(1000);

    for (int i = 0; i < threads.length; ++i) {
      threads[i].interrupt();
    }
    for (int i = 0; i < threads.length; ++i) {
      synchronized (threads[i]) {
        threads[i].join();
      }
    }

    assertThat(throwables.size(), is(0));
    assertThat(counter[0], greaterThanOrEqualTo(threads.length));
  }
}
