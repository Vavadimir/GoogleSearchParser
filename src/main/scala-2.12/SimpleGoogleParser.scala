/**
  * Created by on 2/20/2017.
  */
class SimpleGoogleParser {
  import org.jsoup._
  import scala.collection.JavaConversions._

  object SimpleGoogleParser {
    def main(args: Array[String]): Unit = {
      System.setProperty("http.proxyHost", "xxx.xxx.xxx")
      /** val doc = Jsoup.connect("http://google.com.ua/search?q=просто+запрос")
      .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
      .get()*/

      val doc=Jsoup.connect("http://www.google.com/search?q=просто+запрос&num=100").userAgent("Chrome").get()
      println(doc)
      /** val doc = Jsoup.connect("http://google.com.ua/search?q=просто+запрос").get() */
      val elems = doc.select("h3 > a ")
      for (e <- elems) println(e.attr("href").drop(7))
    }
  }
}
