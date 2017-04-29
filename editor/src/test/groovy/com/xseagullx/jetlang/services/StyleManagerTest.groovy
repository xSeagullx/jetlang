package com.xseagullx.jetlang.services

import spock.lang.Specification
import spock.lang.Unroll

import javax.swing.text.StyleConstants
import java.awt.Color

class StyleManagerTest extends Specification {
	def "test colors"() {
		when:
		def styles = ConfigService.parseConfig(new StringReader(testJson)).styles
		def manager = StyleManager.create(styles)

		then:
		manager.backgroundColor == Color.RED
		manager.foregroundColor == Color.BLUE
		manager.caretColor == Color.GREEN
	}

	@Unroll("Attribute: #attributeName is set to #attributeValue")
	def "test style"() {
		setup:
		def manager = new StyleManager(Color.BLACK, Color.WHITE, Color.WHITE)
		def style = new StyleBean(configParams)

		when:
		manager.configureStyle("main", style)

		then:
		manager.main.getAttribute(attributeName) == attributeValue

		where:
		configParams                 || attributeName               | attributeValue
		[fontFamily: "default"]      || StyleConstants.FontFamily   | "Inconsolata LGC"
		[fontFamily: "Courier New"]  || StyleConstants.FontFamily   | "Courier New"
		[fontSize: 20]               || StyleConstants.FontSize     | 20
		[underline: true]            || StyleConstants.Underline    | true
		[bold: true]                 || StyleConstants.Bold         | true
		[foreground: '#ff00']        || StyleConstants.Foreground   | Color.GREEN
		[background: '#ff']          || StyleConstants.Background   | Color.BLUE
	}

	def "test parent styles"() {
		setup:
		def manager = new StyleManager(Color.BLACK, Color.WHITE, Color.WHITE)
		def mainStyle = new StyleBean(foreground: "#ffff00", background: "#ff")
		def errorStyle = new StyleBean(foreground: "#ff0000", parent: "main")

		when:
		manager.configureStyle("main", mainStyle)
		manager.configureStyle("error", errorStyle)

		then: "background is inherited"
		manager.error.getAttribute(StyleConstants.Background) == Color.BLUE

		and: "foreground is overriden"
		manager.error.getAttribute(StyleConstants.Foreground) == Color.RED

		and: "parent style is intact"
 		manager.main.getAttribute(StyleConstants.Foreground) == Color.YELLOW
	}

	private static String getTestJson() {
		//language=json
		return '''{
			"styles": {
				"backgroundColor": "#ff0000",
				"caretColor": "#00ff00",
				"foregroundColor": "#ff",
				"main": {},
				"keyword": {},
				"string": {},
				"number": {},
				"error": {}
			}
		}'''
	}
}
