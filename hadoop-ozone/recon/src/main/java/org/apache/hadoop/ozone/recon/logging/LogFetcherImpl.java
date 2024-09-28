package org.apache.hadoop.ozone.recon.logging;

import org.apache.hadoop.ozone.recon.logging.LogModels.LogEvent;
import org.apache.hadoop.ozone.recon.logging.LogModels.LoggerResponse;
import org.apache.hadoop.ozone.recon.logging.LogReaders.LogEventReader;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

/**
 * This class will be used to implement the API call actions to /log endpoint
 * We might have new data in the file, so we open the Reader in each method
 * and again close it at the end
 */
public class LogFetcherImpl implements LogFetcher {

  private final LogEventReader logEventReader;

  public LogFetcherImpl() {
    logEventReader = new LogEventReader();
  }

  /**
   * Method to initialize the reader with the location of the file
   * @param location Stores the location of the file
   * @throws IOException if something went wrong during I/O operations
   * @throws LogFileEmptyException if the log file is empty
   */
  public void initializeReader(String location) throws IOException, LogFileEmptyException {
    logEventReader.initializeReader(location);
  }

  /**
   * Get the logs from a given offset
   * @param offset     The offset of the log line
   * @param direction  The direction towards which we fetch
   *                   log data
   * @param events     The number of log events to fetch
   * @return {@link LoggerResponse.Builder} instance of the events
   * @throws IOException in case of error in I/O operations
   * @throws ParseException in case of error while parsing log event timestamp
   * @throws NullPointerException if unable to fetch events
   */
  @Override
  public LoggerResponse.Builder getLogs(long offset, Direction direction, int events)
      throws IOException, ParseException, NullPointerException {

    // Fetch the events
    Deque<LogEvent> logEventDeque = new LinkedList<>();
    // Fetch the event at offset
    logEventDeque.add(logEventReader.getEventAt(offset));

    for (int idx = 1; idx < events; idx++) {
      LogEvent event = null;
      if (Direction.FORWARD == direction) {
        event = logEventReader.getNextEvent();
        // Did not find any event so assume end of events
        if (null == event) {
          break;
        }
        logEventDeque.add(event);
      }

      if (Direction.REVERSE == direction) {
        event = logEventReader.getPrevEvent();
        // Did not find any event so assume end of events
        if (null == event) {
          break;
        }
        logEventDeque.addFirst(event);
      }
    }

    // throws NPE if events are not found
    long firstEventOffset = logEventDeque.getFirst().getOffset();
    long lastEventOffset = logEventDeque.getLast().getOffset();
    return LoggerResponse.newBuilder()
      .setLogs(new ArrayList<>(logEventDeque))
      .setFirstOffset(firstEventOffset)
      .setLastOffset(lastEventOffset);
  }

  /**
   * Get the last events number of events
   * This is the default implementation for initial fetch of data
   * We will start from the end of the logfile for the most recent event
   * @param events Stores the number of events to get
   * @return {@link LoggerResponse.Builder} instance of the events from the end
   * @throws IOException if some I/O operation gave error
   * @throws ParseException if unable to parse date/time
   * @throws NullPointerException if unable to fetch events
   */
  public LoggerResponse.Builder getLogs(int events)
      throws IOException, ParseException, NullPointerException, LogFileEmptyException {

    Deque<LogEvent> logEventDeque = new LinkedList<>();
    LogEvent le = logEventReader.getLastEvent();

    logEventDeque.add(le);

    for (int idx = 1; idx < events; idx++) {
      LogEvent event = logEventReader.getPrevEvent();

      //Did not find any event so assume end of events
      if (null == event) {
        break;
      }
      // Since we are reading in reverse we need to add the events before current event
      logEventDeque.addFirst(event);
    }

    //throws NPE here if the events are not found
    long firstEventOffset = logEventDeque.getFirst().getOffset();
    long lastEventOffset = logEventDeque.getLast().getOffset();
    return LoggerResponse.newBuilder()
      .setLogs(new ArrayList<>(logEventDeque))
      .setFirstOffset(firstEventOffset)
      .setLastOffset(lastEventOffset);
  }

  public void close() throws IOException {
    logEventReader.close();
  }

}
