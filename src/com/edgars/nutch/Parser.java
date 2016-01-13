package com.edgars.nutch;

import com.edgars.table.Author;
import com.edgars.table.Category;
import com.edgars.table.PrintBook;
import com.edgars.table.Table;
import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.NotFound;
import com.jaunt.ResponseException;
import com.jaunt.UserAgent;
import json.JSONObject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.parse.Parse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;

/**
 * CHECK IF JAUNT-API IS UP-TO-DATE!!! OTHERWISE CRAWLING PROCESS WILL BE
 * FAILED! "JAUNT HAS EXPIRED! [<a href="http://jaunt-api.com">http://jaunt-api.com</a>]"
 * Jaunt is freely available for personal and commercial use under the following license.
 * Both the software and the license expire at the end of every month, at which point the most recent version should be downloaded.
 * Change jaunt library into Nutch (%NUTCH_HOME%/plugins/index-njp). Rewrite plugin.xml
 * (%NUTCH_HOME%/plugins/index-njp). Changing
 * <library name="jauntOldVersion.jar"/> to <library name="jauntNewVersion.jar"/>.
 * A Parser plugin for Nutch. Implements interface IndexingFilter. Extension
 * point for indexing. Permits one to add metadata to the indexed fields. All
 * plugins found which implement this extension point are run sequentially on
 * the parse. Uses Jaunt 1.0.0.1 as external .jar library. Parses HTML document
 * by given url. HTML tags are specified for website: <a href="https://www.manoknyga.lt">Manoknyga.lt</a>
 * Created by Edgars on 09/11/2015.
 * Email: edgars_fjodorovs@inbox.lv
 */
public class Parser implements IndexingFilter {

    /**
     * Log constant for output logs in <i>%NUTCH_HOME%/logs</i> directory.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

    /**
     * title variable to store book's title.
     */
    String title = "";
    /**
     * price variable to store book's price.
     */
    String price = "";
    /**
     * oldPrice variable to store book's old price.
     */
    String oldPrice = "";
    /**
     * summary variable to store book's description.
     */
    String summary = "";
    /**
     * tagTitle variable to help parsing tags.
     */
    String tagTitle = "";
    /**
     * tag variable to help parsing tags.
     */
    String tag = "";
    /**
     * pages variable to store book's page count.
     */
    String pages = "";
    /**
     * publisher variable to store book's publisher.
     */
    String publisher = "";
    /**
     * publishDate variable to store book's publish date.
     */
    String publishDate = "";
    /**
     * translator variable to store book's translator.
     */
    String translator = "";
    /**
     * cover variable to store book's cover.
     */
    String cover = "";
    /**
     * isbn variable to store book's isbn.
     */
    String isbn = "";
    /**
     * img variable to store book's img (url to image).
     */
    String img = "";
    /**
     * category variable to help parse book's categories.
     */
    String category = "";
    /**
     * author variable to help parse book's authors.
     */
    String author = "";

    /**
     * Hadoop configuration variable. Configurations are specified by resources.
     * A resource contains a set of name/value pairs as XML data. Each resource
     * is named by either a String or by a Path. If named by a String, then the
     * classpath is examined for a file with that name. If named by a Path, then
     * the local filesystem is examined directly, without referring to the
     * classpath. Used in get/set methods in bottom of the code.
     * <a href="https://hadoop.apache.org/docs/stable/api/org/apache/hadoop/conf/Configuration.html">Configuration</a>
     */
    private Configuration conf;

    /**
     * InterfaceAudience.Public InterfaceStability.Stable public interface
     * Configurable Something that may be configured with a Configuration.
     * <a href="https://hadoop.apache.org/docs/r2.4.1/api/org/apache/hadoop/conf/Configurable.html">Configurable</a>
     */

    /**
     * Return the configuration used by this object.
     *
     * @return conf
     */
    public Configuration getConf() {
        return conf;
    }

    /**
     * Set the configuration to be used by this object.
     *
     * @param conf
     */
    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    /**
     * Method filter which is called by Nutch crawler.
     * <a href="nutch.apache.org/apidocs/apidocs-1.8/org/apache/nutch/indexer/IndexingFilters.html">IndexingFilters</a>
     * about filter method. http://nutch.apache.org/apidocs/apidocs-1.8/
     * <a href="http://wiki.apache.org/nutch/WritingPluginExample">WritingPluginExample</a>
     *
     * @param nutchDocument A NutchDocument is the unit of indexing.
     * @param parse         The result of parsing a page's raw content.
     * @param url           An url which Nutch is currently crawling.
     * @param datum         <a href="http://nutch.apache.org/apidocs/apidocs-1.8/org/apache/nutch/crawl/CrawlDatum.html">CrawlDatum</a>
     * @param inlinks       A list of Inlinks.
     * @return NutchDocument Nutch documentation does not describe anything
     * @throws IndexingException
     */
    public NutchDocument filter(NutchDocument nutchDocument, Parse parse, Text url,
                                CrawlDatum datum, Inlinks inlinks) throws IndexingException {

        /**
         * We'll hold all info about book into json and pass this json to PrintBook's constructor.
         */
        JSONObject json = new JSONObject();

        /**
         * PrintBook object is in table.jar.
         */
        Table printBook = null;

        String link = url.toString();
        json.put("url", link);
        LOG.info("Parsing link " + link);
        System.out.println("Parsing link " + link);

        /**
         * Creating Jaunt UserAgent which will visit urls.
         */
        UserAgent userAgent = new UserAgent();

        try {
            /**
             * Jaunt UserAgent visits an url passed by Nutch crawler.
             */
            userAgent.visit(link);

            /**
             * Going through website's DOM. Getting <i>body</i> at first.
             */
            Element body = userAgent.doc.findFirst("<body>");

            // Checking if there is info_books_item class in the HTML.
            if (body.findEvery("<div class=info_books_item>").size() > 0) {

                // MainInfoBlockDescription block.
                if (body.findEvery("<div class=main_infoblock_description")
                        .size() > 0) {
                    Element mainInfoblockDescription = body
                            .findFirst("<div class=main_infoblock_description");

                    if (mainInfoblockDescription
                            .findEvery("<div class=item_tags>")
                            .findEvery("div").size() > 0) {

                        // Book Tags.
                        Elements tagElements = mainInfoblockDescription
                                .findFirst("<div class=item_tags>")
                                .findEvery("div");

                        for (Element tagName : tagElements) {
                            /**
                             * Checking if in <i>span</i> is urls.
                             */
                            if (tagName.findEvery("span").findEach("a")
                                    .size() > 0) {
                                tagTitle = tagName.getText()
                                        .replace(": ", "").replace(" ", "")
                                        .toLowerCase();
                                tag = tagName.findFirst("span")
                                        .findFirst("a").getText();
                            } else {
                                tagTitle = tagName.getText()
                                        .replace(": ", "").replace(" ", "")
                                        .toLowerCase();
                                tag = tagName.findFirst("span").getText()
                                        .replace("&nbsp;", " ");
                            }

                            if (tagTitle.equals("isbnkodas")) {
                                isbn = tagName.findFirst("span")
                                        .getText()
                                        .replace("&nbsp;", " ")
                                        .replace("ISBN", "")
                                        .replace(" ", "");
                            }
                            if (tagTitle.equals("puslapiai")) {
                                pages = tag;
                            }

                            if (tagTitle.equals("leidykla")) {
                                publisher = tag;
                            }
                            if ((URLEncoder.encode(tagTitle, "UTF-8").equals("i%C5%A1leista"))) {
                                publishDate = tag;
                            }

                            if (URLEncoder.encode(tagTitle, "UTF-8").equals("vir%C5%A1elis")) {
                                cover = tag;
                            }

                            if (URLEncoder.encode(tagTitle, "UTF-8").equals("vert%C4%97jas")) {
                                translator = tag;
                            }
                        }
                    }
                    json.put("isbn", isbn);
                    json.put("pages", pages);
                    json.put("publisher", publisher);
                    json.put("publishDate", publishDate);
                    json.put("translator", translator);
                    json.put("cover", cover);

                    if (mainInfoblockDescription
                            .findFirst("<span itemprop=description>")
                            .findEvery("p").size() > 0) {

                        // Making Elements of each containing Author
                        Elements abstractElement = mainInfoblockDescription
                                .findFirst("<span itemprop=description>")
                                .findEvery("p");

                        // Looping through all Descriptions.
                        for (Element description : abstractElement) {

                            if (description.findEvery("b").size() > 0) {

                                /**
                                 * Checking for <p> tags with description.
                                 * Empty <p> tags are also here,
                                 * but at concatenation they are not shown.
                                 */
                            } else if (description.getText().length() != 0) {
                                summary += description.getText();
                            }
                        }

                    }
                    json.put("summary", summary);

                    if (body.findEvery("<div class=info_books_item>")
                            .size() > 0) {

                        /**
                         * <i>div class=info_books_item</i> contains all information about the book.
                         */
                        Element infoBooksItem = body
                                .findFirst("<div class=info_books_item>");

                        if (infoBooksItem.findEvery(
                                "<div class=info_books_item_information")
                                .size() > 0) {

                            /**
                             * <i>div class=info_books_item_information</i> block
                             * contains such information as: authors, book
                             * name, price, discounts, eBook link, rating,
                             * social buttons etc.
                             */
                            Element infoBooksItemInformation = infoBooksItem
                                    .findFirst("<div class=info_books_item_information");

                            if (infoBooksItemInformation.findEvery("h1")
                                    .findEvery("a").size() > 0) {

                                // Getting name of the book.
                                title = infoBooksItemInformation
                                        .findFirst("h1").findFirst("a")
                                        .getText();
                            }
                            json.put("title", title);

                            if (infoBooksItemInformation
                                    .findEvery("<div class=prices>")
                                    .findEvery("<div itemprop=price>")
                                    .size() > 0) {
                                /**
                                 * Getting price of the book. Removing <i>nbsp</i>;
                                 */
                                price = infoBooksItemInformation
                                        .findFirst("<div class=prices>")
                                        .findFirst("<div itemprop=price>")
                                        .getText();
                                price = price.replace("&nbsp;", " ");

                            }
                            json.put("price", price);

                            if (infoBooksItemInformation
                                    .findEvery("<div class=prices>")
                                    .findEvery("<div class=text>")
                                    .findEvery("span").size() > 0) {

                                /**
                                 * Getting old price of the book. Removing <i>nbsp</i>;
                                 */
                                oldPrice = infoBooksItemInformation
                                        .findFirst("<div class=prices>")
                                        .findFirst("<div class=text>")
                                        .findFirst("span").getText();
                                oldPrice = oldPrice.replace("&nbsp;", " ");
                            }
                            json.put("oldPrice", oldPrice);

                            if (infoBooksItem.findEvery(
                                    "<div class=info_books_item_images>")
                                    .size() > 0) {

                                /**
                                 * Image is on another block <i>div class=info_books_item_images</i>.
                                 * Getting image.
                                 */
                                Element infoBooksItemImages = infoBooksItem
                                        .findFirst("<div class=info_books_item_images>");
                                img = infoBooksItemImages
                                        .findFirst("a").findFirst("img")
                                        .getAttx("src");

                            }
                            json.put("img", img);

                            try {
                                printBook = new PrintBook(json);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            if (infoBooksItemInformation
                                    .findEvery("<div class=authors>")
                                    .findEvery("h2").findEvery("a").size() > 0) {

                                // Making Elements of each containing Author
                                Elements authorElement = infoBooksItemInformation
                                        .findEach("<div class=authors>")
                                        .findEvery("h2").findEvery("a");

                                // Looping through all Authors
                                for (int i = 0; i < authorElement.size(); i++) {
                                    author = authorElement.toList().get(i)
                                            .getText();
                                    /**
                                     * Splitting author by name (0) and surname (1).
                                     */
                                    String[] a = author.split(" ");
                                    String name = "";
                                    String surname = "";
                                    if (a.length < 2) {
                                        name = a[0];
                                    } else {
                                        name = a[0];
                                        surname = a[1];
                                    }
                                    printBook = new Author(printBook, name, surname);
                                }
                            } else {
                                printBook = new Author(printBook);
                            }

                            if (infoBooksItem
                                    .findEvery(
                                            "<div class=main_categories_navigation>")
                                    .size() > 0) {

                                // Getting categories.
                                Element mainCategoriesNavigation = infoBooksItem
                                        .findFirst("<div class=main_categories_navigation>");

                                // Finding all categories
                                Elements categoriesElement = mainCategoriesNavigation
                                        .findEach("ul").findEach("li")
                                        .findEach("a");

                                // Looping through all categories
                                for (int i = 0; i < categoriesElement
                                        .size(); i++) {
                                    category = categoriesElement.toList().get(i)
                                            .getText();
                                    printBook = new Category(printBook, category);
                                }
                            }
                        }
                    }
                    try {
                        /**
                         * Saving everything.
                         * Book, Authors, Categories.
                         */
                        printBook.save();
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                /**
                 * This means that link does not have a book. There is no <i>div class=info_books_item</i> tag.
                 */
                System.out.println("<div class=info_books_item> is null");
            }

        } catch (ResponseException | NotFound e) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nutchDocument;
    }
}
