/*
 * File:    $HeadURL:  $
 * Version: $LastChangedRevision: 643 $
 * Date:    $Date: $
 * Author:  $LastChangedBy: $
 *
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2014 JVoiceXML group - http://jvoicexml.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.jvoicexml.callmanager.mmi.json;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.jvoicexml.LastResult;
import org.jvoicexml.RecognitionResult;
import org.jvoicexml.SpeakableText;
import org.jvoicexml.callmanager.mmi.ConversionException;
import org.jvoicexml.callmanager.mmi.ExtensionNotificationDataConverter;
import org.jvoicexml.event.error.SemanticError;
import org.jvoicexml.event.plain.implementation.OutputEndedEvent;
import org.jvoicexml.event.plain.implementation.OutputStartedEvent;
import org.jvoicexml.event.plain.implementation.RecognitionEvent;
import org.jvoicexml.event.plain.implementation.SpokenInputEvent;
import org.jvoicexml.event.plain.implementation.SynthesizedOutputEvent;
import org.jvoicexml.interpreter.datamodel.DataModel;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Converts the extension notifications into the JSON format.
 * 
 * @author Dirk Schnelle-Walka
 * @version $Revision: $
 * @since 0.7.7
 */
public class JsonExtensionNotificationDataConverter
        implements ExtensionNotificationDataConverter {
    /** Logger instance. */
    private static final Logger LOGGER = Logger
            .getLogger(JsonExtensionNotificationDataConverter.class);

    /**
     * Constructs a new object.
     */
    public JsonExtensionNotificationDataConverter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object convertApplicationLastResult(
            final List<LastResult> lastresults) throws ConversionException {
        if (lastresults == null || lastresults.isEmpty()) {
            return null;
        }
        final Context context = Context.enter();
        context.setLanguageVersion(Context.VERSION_DEFAULT);
        final ScriptableObject scope = context.initStandardObjects();
        final LastResult lastresult = lastresults.get(0);
        final Scriptable vxml = context.newObject(scope);
        ScriptableObject.putProperty(vxml, "utterance",
                lastresult.getUtterance());
        ScriptableObject.putProperty(vxml, "confidence",
                lastresult.getConfidence());
        ScriptableObject.putProperty(vxml, "mode", lastresult.getInputmode());
        ScriptableObject.putProperty(vxml, "interpretation",
                lastresult.getInterpretation());
        return toJSON(vxml);
    }

    /**
     * Packs the given SSML document into a JSON structure.
     * 
     * @param speakable
     *            the speakable with the SSML document
     * @return JSON formatted string
     */
    private String toJson(final SpeakableText speakable) {
        final Context context = Context.enter();
        context.setLanguageVersion(Context.VERSION_DEFAULT);
        final ScriptableObject scope = context.initStandardObjects();
        final String ssml = speakable.getSpeakableText();
        final Scriptable vxml = context.newObject(scope);
        ScriptableObject.putProperty(vxml, "ssml", ssml);
        return toJSON(vxml);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object convertSynthesizedOutputEvent(
            final SynthesizedOutputEvent event) throws ConversionException {
        if (event instanceof OutputStartedEvent) {
            final OutputStartedEvent started = (OutputStartedEvent) event;
            final SpeakableText speakable = started.getSpeakable();
            return toJson(speakable);
        } else if (event instanceof OutputEndedEvent) {
            final OutputEndedEvent ended = (OutputEndedEvent) event;
            final SpeakableText speakable = ended.getSpeakable();
            return toJson(speakable);
        }
        LOGGER.info("not sending synthesized output event: " + event);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object convertRecognitionEvent(final DataModel model,
            final RecognitionEvent event) throws ConversionException {
        final RecognitionResult result = event.getRecognitionResult();
        final Context context = Context.enter();
        context.setLanguageVersion(Context.VERSION_DEFAULT);
        final ScriptableObject scope = context.initStandardObjects();
        final Scriptable vxml = context.newObject(scope);
        ScriptableObject.putProperty(vxml, "utterance", result.getUtterance());
        ScriptableObject
                .putProperty(vxml, "confidence", result.getConfidence());
        ScriptableObject.putProperty(vxml, "mode", result.getMode());
        try {
            ScriptableObject.putProperty(vxml, "interpretation",
                    result.getSemanticInterpretation(model));
        } catch (SemanticError e) {
            throw new ConversionException(e.getMessage(), e);
        }
        return toJSON(vxml);
    }

    /**
     * {@inheritDoc}
     * 
     * Does nothing since no data is required
     */
    @Override
    public Object convertSpokenInputEvent(final SpokenInputEvent input)
            throws ConversionException {
        return null;
    }

    /**
     * Transforms the given {@link ScriptableObject} into a JSON string.
     * 
     * @param object
     *            the object to serialize
     * @return JSON formatted string
     * @since 0.7.5
     */
    public String toJSON(final Scriptable object) {
        final JSONObject json = toJSONObject(object);
        if (json == null) {
            return null;
        }
        return json.toJSONString();
    }

    /**
     * Transforms the given {@link ScriptableObject} into a JSON object.
     * 
     * @param object
     *            the object to serialize
     * @return JSON object
     * @since 0.7.5
     */
    @SuppressWarnings("unchecked")
    private JSONObject toJSONObject(final Scriptable object) {
        if (object == null) {
            return null;
        }
        final Object[] ids = ScriptableObject.getPropertyIds(object);
        JSONObject json = new JSONObject();
        for (Object id : ids) {
            final String key = id.toString();
            Object value = object.get(key, object);
            if (value instanceof ScriptableObject) {
                final ScriptableObject scriptable = (ScriptableObject) value;
                final JSONObject subvalue = toJSONObject(scriptable);
                json.put(key, subvalue);
            } else {
                json.put(key, value);
            }
        }
        return json;
    }
}
