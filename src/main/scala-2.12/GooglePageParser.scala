import org.htmlcleaner.HtmlCleaner

/**
  * Created by vlad on 18.02.17.
  */
object GooglePageParser extends App with PageParser {

  override def parse(page: String): List[String] = {

    val htmlCleaner = new HtmlCleaner()

    val mainNode = htmlCleaner.clean(page)

    val div1 = mainNode.getElementsByAttValue("class", "g", true, true)

    val div2 = div1.flatMap(i => i.getElementsByName("h3", true))

    val div3 = div2.flatMap(i => i.getElementsByName("a", true))

    div3.map(i => {
      extractUrl(i.getAttributeByName("href"))
    }).toList
  }

  private def extractUrl(value : String) : String = {
    val s1 = value.split(";")(0)
    s1.substring(7, s1.length - 4)
  }
}
