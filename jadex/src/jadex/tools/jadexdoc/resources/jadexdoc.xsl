<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:output method="xml" indent="yes"/>

	<!-- Match elements to transform comments to descriptions. -->
	<xsl:template match="*">
		<xsl:copy>
			<xsl:choose>
				<!-- When element has description and comment: Add comment to description.
					 The expression checks if directly preceding sibling is a comment "[1][self::comment()]"
					 but only considers comments and elements "[self::comment()|self::*]"
					 therefore ignoring directly preceding text() nodes (e.g. due to line breaks). -->
				<xsl:when test="@description and preceding-sibling::node()[self::comment()|self::*][1][self::comment()]">
					<xsl:attribute name="description">
						<xsl:value-of select="concat(@description, ' ', preceding-sibling::node()[self::comment()][1])"/>
						<!--<xsl:value-of select="concat(@description, ' ', normalize-space(preceding-sibling::node()[self::comment()][1]))"/>-->
					</xsl:attribute>
				</xsl:when>
				<!-- When element has only description: Copy. -->
				<xsl:when test="@description">
					<xsl:attribute name="description">
						<xsl:value-of select="@description"/>
					</xsl:attribute>
				</xsl:when>
				<!-- When element has only comment: Transform to description. -->
				<xsl:when test="preceding-sibling::node()[self::comment()|self::*][1][self::comment()]">
					<xsl:attribute name="description">
						<xsl:value-of select="preceding-sibling::node()[self::comment()][1]"/>
						<!--<xsl:value-of select="normalize-space(preceding-sibling::node()[self::comment()][1])"/>-->
					</xsl:attribute>
				</xsl:when>
			</xsl:choose>

			<!-- Copy contents of element. -->
			<xsl:apply-templates select="@*|*|text()|processing-instruction()|comment()"/>
		</xsl:copy>
	</xsl:template>

	<!-- Ignore descriptions to avoid duplicates. -->
	<xsl:template match="@description"/>

	<!-- Copy everything else. -->
	<xsl:template match="@*|text()|processing-instruction()|comment()">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()|processing-instruction()|comment()"/>
		</xsl:copy>
	</xsl:template>

	<!-- Copy elements that must not hold descriptions. -->
	<xsl:template match="imports|import|capabilities|assignto|affected|concrete
		|abstract|unique|exclude|deliberation
		|trigger|trigger/internalevent|trigger/messageevent|trigger/goal
		|waitqueue|waitqueue/internalevent|waitqueue/messageevent|waitqueue/goal
		|servicedescription/ontology|servicedescription/language|servicedescription/property
		|agentdescription/ontology|agentdescription/language|property|service
		|constraint|agent/parameters/parameter/value|agent/parameters/parameterset/value
		|initialstates">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()|processing-instruction()|comment()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>