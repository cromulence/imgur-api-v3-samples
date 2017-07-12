package net.cromulence.imgur.samples;

import net.cromulence.imgur.apiv3.ImgurOAuthUtils;
import net.cromulence.imgur.apiv3.api.Imgur;
import net.cromulence.imgur.apiv3.auth.ImgurOAuthHandler;
import net.cromulence.imgur.apiv3.datamodel.GalleryEntry;
import net.cromulence.imgur.apiv3.datamodel.constants.GallerySort;
import net.cromulence.imgur.apiv3.datamodel.constants.GalleryWindow;
import net.cromulence.imgur.apiv3.datamodel.constants.Section;
import net.cromulence.imgur.apiv3.endpoints.Paginated;

/**
 * Sample app which shows a running count of the number of new posts to usersub each minute
 *
 * In order to use this sample, you need to specify your Imgur Application's client ID and client
 * secret (in the fields IMGUR_CLIENT_ID and IMGUR_CLIENT_SECRET, respectively)
 *
 * The implementation is very naive. OAuth tokens aren't persisted, so you need to log in every time
 * you run it. It uses a post id as a reference point for counting, and if that post is deleted you
 * will end up in an infinite loop. It also doesn't keep track of the time it takes to iterate
 * through the posts to count them, so the time between checks is slightly over a minute. These are
 * simple problems to fix, but are outside the scope of the sample
 */
public class UsersubPpm {

  private static final String IMGUR_CLIENT_ID = "SET THIS";
  private static final String IMGUR_CLIENT_SECRET = "SET THIS";

  public static void main(String[] args) throws Exception {

    // Create an Imgur instance
    Imgur imgur = new Imgur(IMGUR_CLIENT_ID, IMGUR_CLIENT_SECRET);

    // Get the auth handler
    ImgurOAuthHandler ah = imgur.authHandler;

    // If we don't have a token, or if we have one which has expired and can't be refreshed, re-auth
    if(!ah.hasToken() || (ah.isAccessTokenExpired() && !imgur.auth.refreshToken())) {
      ImgurOAuthUtils.getAndExchangePin(imgur, ah);
    }

    String lastId = null;

    do {
      // Get an iterator for usersub entries
      Paginated<GalleryEntry[]> gallery = imgur.gallery.gallery(Section.USER, GallerySort.TIME, GalleryWindow.DAY, false);

      if (lastId == null) {
        // This is the first time through. Make a note of the most recent post
        System.out.println("Found the first post, waiting");
        lastId = gallery.next()[0].getId();
      } else {
        // Go through the posts until we find the one we noted
        int count = 0;
        String newLastId = null;

        for (GalleryEntry ge : gallery.next()) {
          // Make a note of the current most recent post. We need this for next time
          if (newLastId == null) {
            newLastId = ge.getId();
          }

          if (ge.getId().equals(lastId)) {
            break;
          }

          count++;
        }

        lastId = newLastId;
        System.out.println("Posts in the last minute: " + count);
      }

      Thread.sleep(60000);
    } while (true);
  }
}