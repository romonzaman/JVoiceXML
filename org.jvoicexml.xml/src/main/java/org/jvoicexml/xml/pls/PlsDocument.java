/*
 * File:    $HeadURL$
 * Version: $LastChangedRevision$
 * Date:    $Date$
 * Author:  $LastChangedBy$
 *
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2008-2015 JVoiceXML group - http://jvoicexml.sourceforge.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.jvoicexml.xml.pls;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.jvoicexml.xml.PlsNode;
import org.jvoicexml.xml.XmlDocument;
import org.jvoicexml.xml.XmlNodeFactory;
import org.jvoicexml.xml.XmlNodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A PLS XML document according to the specification in <a
 * href="http://www.w3.org/TR/pronunciation-lexicon/">
 * http://www.w3.org/TR/pronunciation-lexicon/</a>.
 *
 * <p>
 * Objects of this class can create PLS XML grammar documents or
 * parse them.
 * </p>
 *
 * @author Dirk Schnelle-Walka
 *
 * @version $Revision$
 * @since 0.6
 */
public final class PlsDocument
        extends XmlDocument {
    /** The serial version UID. */
    private static final long serialVersionUID = -2218143321006476920L;

    /** The <code>XmlNodefactory</code> to use. */
    private static final transient PlsNodeFactory NODE_FACTORY;

    static {
        NODE_FACTORY = new PlsNodeFactory();
    }

    /**
     * Creates an empty PLS Document.
     *
     * @throws ParserConfigurationException
     *         If anything goes wrong while parsing the document.
     */
    public PlsDocument()
            throws ParserConfigurationException {
        super();
    }

    /**
     * Constructs a new PLS document from the given input source.
     *
     * @param source
     *        Input source for a single XML document.
     * @throws ParserConfigurationException
     *         Error creating the document builder.
     * @throws SAXException
     *         Error parsing the input source.
     * @throws IOException
     *         Error reading the input source.
     */
    public PlsDocument(final InputSource source)
            throws ParserConfigurationException, SAXException, IOException {
        super(source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XmlNodeFactory<?> getXmlNodefactory() {
        return NODE_FACTORY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node createRootNode() {
        final Document document = getDocument();
        final Node node = document.createElement(Lexicon.TAG_NAME);
        return new Lexicon(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeList getXmlNodeList(final NodeList nodeList) {
        return new XmlNodeList<PlsNode>(NODE_FACTORY, nodeList);
    }

    /**
     * Get the one and only child of this document: The lexicon node.
     *
     * @return The lexicon child, <code>null</code> if there is
     *         none.
     */
    public Lexicon getLexicon() {
        NodeList lexicon = getElementsByTagName(Lexicon.TAG_NAME);
        if (lexicon.getLength() == 0) {
            lexicon = getElementsByTagNameNS("*", Lexicon.TAG_NAME);
            if (lexicon.getLength() == 0) {
                return null;
            }
        }

        return (Lexicon) lexicon.item(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDefaultNamespaceURI() {
        return "http://www.w3.org/2005/01/pronunciation-lexicon";
    }
}
