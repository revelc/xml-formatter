/*******************************************************************************
 * Copyright (c) 2004, 2011 John-Mason P. Shackelford and others.,
 *               2019, 2021 Jose Montoya
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John-Mason P. Shackelford - initial API and implementation
 * 	   IBM Corporation - bug fixes
 * 	   Jose Montoya - Modified implementation outside Eclipse Platform
 * 	                - Add thread safety to TagReaderFactory
 *******************************************************************************/
package net.revelc.code.formatter.xml.lib;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class XmlDocumentFormatter {

    private final String fDefaultLineDelimiter;
    private final FormattingPreferences prefs;

    /**
     * Track the format state separately for each new call to {@link XmlDocumentFormatter#format(String)}, so the
     * {@link XmlDocumentFormatter} object can be reused.
     */
    private static class FormatState {
        private int depth = 0;
        private boolean lastNodeWasText = false;
        private StringBuilder out = new StringBuilder(200);
    }

    public XmlDocumentFormatter() {
        this(System.lineSeparator(), new FormattingPreferences());
    }

    public XmlDocumentFormatter(FormattingPreferences prefs) {
        this(System.lineSeparator(), prefs);
    }

    public XmlDocumentFormatter(String defaultLineDelimiter, FormattingPreferences prefs) {
        this.fDefaultLineDelimiter = defaultLineDelimiter;
        this.prefs = prefs;
    }

    private void copyNode(Reader reader, FormatState state) throws IOException {
        TagReader tag = TagReaderFactory.createTagReaderFor(reader);
        state.depth += tag.getPreTagDepthModifier();

        if (!state.lastNodeWasText) {

            if (tag.startsOnNewline() && !hasNewlineAlready(state)) {
                state.out.append(fDefaultLineDelimiter);
            }

            if (tag.requiresInitialIndent()) {
                indent(state.depth, state.out);
            }
        }

        if (tag instanceof XmlElementReader) {
            StringBuilder indentBuilder = new StringBuilder(30);
            indent(state.depth, indentBuilder);
            state.out.append(new XMLTagFormatter().format(tag.getTagText(), indentBuilder.toString(),
                    fDefaultLineDelimiter, prefs));
        } else if (tag instanceof CommentReader) {
            StringBuilder indentBuilder = new StringBuilder(30);
            indent(state.depth, indentBuilder);
            state.out.append(new CommentFormatter().format(tag.getTagText(), indentBuilder.toString(),
                    fDefaultLineDelimiter));
        } else {
            String tagText = tag.getTagText();
            if (!prefs.getDeleteBlankLines()
                    || (prefs.getDeleteBlankLines() && tagText != null && !tagText.isBlank())) {
                state.out.append(tagText);
            }
        }

        state.depth += tag.getPostTagDepthModifier();
        state.lastNodeWasText = tag.isTextNode();
    }

    public String format(String documentText) {
        if (!prefs.getWellFormedValidation().equals(FormattingPreferences.IGNORE)) {
            validateWellFormedness(documentText);
        }

        Reader reader = new StringReader(documentText);
        FormatState state = new FormatState();

        try {
            while (true) {
                reader.mark(1);
                int intChar = reader.read();
                reader.reset();

                if (intChar == -1) {
                    break;
                }
                copyNode(reader, state);
            }
            reader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return state.out.toString();
    }

    private void validateWellFormedness(String documentText) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            SAXParser parser = factory.newSAXParser();
            parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            XMLReader reader = parser.getXMLReader();
            reader.setErrorHandler(errorHandler);
            reader.parse(new InputSource(new StringReader(documentText)));
        } catch (Exception exception) {
            if (!(exception instanceof SAXParseException)
                    || !prefs.getWellFormedValidation().equals(FormattingPreferences.WARN)) {
                throw new IllegalArgumentException(exception);
            }
            System.err.println("WARN: " + exception.getMessage());
        }
    }

    private static boolean hasNewlineAlready(FormatState state) {
        return state.out.lastIndexOf("\n") == state.out.length() - 1 //$NON-NLS-1$
                || state.out.lastIndexOf("\r") == state.out.length() - 1; //$NON-NLS-1$
    }

    private void indent(int depth, StringBuilder out) {
        IntStream.range(0, depth).forEach(i -> out.append(prefs.getCanonicalIndent()));
    }

    private static class CommentReader extends TagReader {

        private boolean complete = false;

        @Override
        protected void clear() {
            this.complete = false;
        }

        @Override
        public String getStartOfTag() {
            return "<!--"; //$NON-NLS-1$
        }

        @Override
        protected String readTag() throws IOException {
            int intChar;
            char c;
            StringBuilder node = new StringBuilder();

            while (!complete && (intChar = reader.read()) != -1) {
                c = (char) intChar;

                node.append(c);

                if (c == '>' && node.toString().endsWith("-->")) { //$NON-NLS-1$
                    complete = true;
                }
            }
            return node.toString();
        }

        @Override
        public boolean requiresInitialIndent() {
            return false;
        }
    }

    private static class DoctypeDeclarationReader extends TagReader {

        private boolean complete = false;

        @Override
        protected void clear() {
            this.complete = false;
        }

        @Override
        public String getStartOfTag() {
            return "<!"; //$NON-NLS-1$
        }

        @Override
        protected String readTag() throws IOException {
            int intChar;
            char c;
            StringBuilder node = new StringBuilder();

            while (!complete && (intChar = reader.read()) != -1) {
                c = (char) intChar;

                node.append(c);

                if (c == '>') {
                    complete = true;
                }
            }
            return node.toString();
        }

    }

    private static class ProcessingInstructionReader extends TagReader {

        private boolean complete = false;

        @Override
        protected void clear() {
            this.complete = false;
        }

        @Override
        public String getStartOfTag() {
            return "<?"; //$NON-NLS-1$
        }

        @Override
        protected String readTag() throws IOException {
            int intChar;
            char c;
            StringBuilder node = new StringBuilder();

            while (!complete && (intChar = reader.read()) != -1) {
                c = (char) intChar;

                node.append(c);

                if (c == '>' && node.toString().endsWith("?>")) { //$NON-NLS-1$
                    complete = true;
                }
            }
            return node.toString();
        }
    }

    private abstract static class TagReader {

        protected Reader reader;

        private String tagText;

        protected abstract void clear();

        public int getPostTagDepthModifier() {
            return 0;
        }

        public int getPreTagDepthModifier() {
            return 0;
        }

        public abstract String getStartOfTag();

        public String getTagText() {
            return this.tagText;
        }

        public boolean isTextNode() {
            return false;
        }

        protected abstract String readTag() throws IOException;

        public boolean requiresInitialIndent() {
            return true;
        }

        public void setReader(Reader reader) throws IOException {
            this.reader = reader;
            this.clear();
            this.tagText = readTag();
        }

        public boolean startsOnNewline() {
            return true;
        }
    }

    private static class TagReaderFactory {
        private static final Map<String, Supplier<TagReader>> tagReaders;

        static {
            tagReaders = new LinkedHashMap<>(4);
            // Warning: the order of the selection is important
            tagReaders.put("<!--", CommentReader::new);
            tagReaders.put("<!", DoctypeDeclarationReader::new);
            tagReaders.put("<?", ProcessingInstructionReader::new);
            tagReaders.put("<", XmlElementReader::new);
        }

        public static TagReader createTagReaderFor(Reader reader) throws IOException {

            char[] buf = new char[10];
            reader.mark(10);
            reader.read(buf, 0, 10);
            reader.reset();

            String startOfTag = String.valueOf(buf);

            for (Map.Entry<String, Supplier<TagReader>> entry : tagReaders.entrySet()) {
                if (startOfTag.startsWith(entry.getKey())) {
                    TagReader tagReader = entry.getValue().get();
                    tagReader.setReader(reader);
                    return tagReader;
                }
            }
            // else
            TagReader textNodeReader = new TextReader();
            textNodeReader.setReader(reader);
            return textNodeReader;
        }
    }

    private static class TextReader extends TagReader {

        private boolean complete;

        private boolean isTextNode;

        @Override
        protected void clear() {
            this.complete = false;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.ant.internal.ui.editor.formatter.XmlDocumentFormatter.TagReader# getStartOfTag()
         */
        @Override
        public String getStartOfTag() {
            return "";
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.ant.internal.ui.editor.formatter.XmlDocumentFormatter.TagReader# isTextNode()
         */
        @Override
        public boolean isTextNode() {
            return this.isTextNode;
        }

        @Override
        protected String readTag() throws IOException {

            StringBuilder node = new StringBuilder();

            while (!complete) {

                reader.mark(1);
                int intChar = reader.read();
                if (intChar == -1) {
                    break;
                }

                char c = (char) intChar;
                if (c == '<') {
                    reader.reset();
                    complete = true;
                } else {
                    node.append(c);
                }
            }

            // if this text node is just whitespace
            // remove it, except for the newlines.
            if (node.length() < 1) {
                this.isTextNode = false;

            } else if (node.toString().trim().length() == 0) {
                String whitespace = node.toString();
                node = new StringBuilder();
                for (int i = 0; i < whitespace.length(); i++) {
                    char whitespaceCharacter = whitespace.charAt(i);
                    if (whitespaceCharacter == '\n' || whitespaceCharacter == '\r') {
                        node.append(whitespaceCharacter);
                    }
                }
                this.isTextNode = false;

            } else {
                this.isTextNode = true;
            }
            return node.toString();
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.ant.internal.ui.editor.formatter.XmlDocumentFormatter.TagReader# requiresInitialIndent()
         */
        @Override
        public boolean requiresInitialIndent() {
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.ant.internal.ui.editor.formatter.XmlDocumentFormatter.TagReader# startsOnNewline()
         */
        @Override
        public boolean startsOnNewline() {
            return false;
        }
    }

    private static class XmlElementReader extends TagReader {

        private boolean complete = false;

        @Override
        protected void clear() {
            this.complete = false;
        }

        @Override
        public int getPostTagDepthModifier() {
            if (getTagText().endsWith("/>") || getTagText().endsWith("/ >") || getTagText().startsWith("</")) { //$NON-NLS-1$
                return 0;
            }
            return +1;
        }

        @Override
        public int getPreTagDepthModifier() {
            if (getTagText().startsWith("</")) { //$NON-NLS-1$
                return -1;
            }
            return 0;
        }

        @Override
        public String getStartOfTag() {
            return "<"; //$NON-NLS-1$
        }

        @Override
        protected String readTag() throws IOException {

            StringBuilder node = new StringBuilder();

            boolean insideQuote = false;
            int intChar;

            while (!complete && (intChar = reader.read()) != -1) {
                char c = (char) intChar;

                node.append(c);
                // TODO logic incorrectly assumes that " is quote character
                // when it could also be '
                if (c == '"') {
                    insideQuote = !insideQuote;
                }
                if (c == '>' && !insideQuote) {
                    complete = true;
                }
            }
            return node.toString();
        }
    }

    private static ErrorHandler errorHandler = new ErrorHandler() {
        @Override
        public void warning(SAXParseException e) throws SAXException {
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    };

}
