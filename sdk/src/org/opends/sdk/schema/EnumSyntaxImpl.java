/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2009 Sun Microsystems, Inc.
 */

package org.opends.sdk.schema;



import static com.sun.opends.sdk.messages.Messages.*;
import static com.sun.opends.sdk.util.StringPrepProfile.*;
import static org.opends.sdk.schema.SchemaConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opends.sdk.ByteSequence;
import org.opends.sdk.ByteString;
import org.opends.sdk.LocalizableMessage;
import org.opends.sdk.LocalizableMessageBuilder;

import com.sun.opends.sdk.util.Validator;



/**
 * This class provides an enumeration-based mechanism where a new syntax
 * and its corresponding matching rules can be created on-the-fly. An
 * enum syntax is an LDAPSyntaxDescriptionSyntax with X-ENUM extension.
 */
final class EnumSyntaxImpl extends AbstractSyntaxImpl
{
  private final String oid;
  // Set of read-only enum entries.
  private final List<String> entries;



  EnumSyntaxImpl(String oid, List<String> entries)
  {
    Validator.ensureNotNull(oid, entries);
    this.oid = oid;
    final List<String> entryStrings =
        new ArrayList<String>(entries.size());

    for (final String entry : entries)
    {
      final String normalized = normalize(ByteString.valueOf(entry));
      if (!entryStrings.contains(normalized))
      {
        entryStrings.add(normalized);
      }
    }
    this.entries = Collections.unmodifiableList(entryStrings);
  }



  @Override
  public String getApproximateMatchingRule()
  {
    return AMR_DOUBLE_METAPHONE_OID;
  }



  public Iterable<String> getEntries()
  {
    return entries;
  }



  @Override
  public String getEqualityMatchingRule()
  {
    return EMR_CASE_IGNORE_OID;
  }



  public String getName()
  {
    return oid;
  }



  @Override
  public String getOrderingMatchingRule()
  {
    return OMR_OID_GENERIC_ENUM + "." + oid;
  }



  @Override
  public String getSubstringMatchingRule()
  {
    return SMR_CASE_IGNORE_OID;
  }



  public int indexOf(ByteSequence value)
  {
    return entries.indexOf(normalize(value));
  }



  public boolean isHumanReadable()
  {
    return true;
  }



  public boolean valueIsAcceptable(Schema schema, ByteSequence value,
      LocalizableMessageBuilder invalidReason)
  {
    // The value is acceptable if it belongs to the set.
    final boolean isAllowed = entries.contains(normalize(value));

    if (!isAllowed)
    {
      final LocalizableMessage message =
          WARN_ATTR_SYNTAX_LDAPSYNTAX_ENUM_INVALID_VALUE.get(value
              .toString(), oid);
      invalidReason.append(message);
    }

    return isAllowed;
  }



  private String normalize(ByteSequence value)
  {
    final StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, TRIM, CASE_FOLD);

    final int bufferLength = buffer.length();
    if (bufferLength == 0)
    {
      if (value.length() > 0)
      {
        // This should only happen if the value is composed entirely of
        // spaces. In that case, the normalized value is a single space.
        return " ";
      }
      else
      {
        // The value is empty, so it is already normalized.
        return "";
      }
    }

    // Replace any consecutive spaces with a single space.
    for (int pos = bufferLength - 1; pos > 0; pos--)
    {
      if (buffer.charAt(pos) == ' ')
      {
        if (buffer.charAt(pos - 1) == ' ')
        {
          buffer.delete(pos, pos + 1);
        }
      }
    }

    return buffer.toString();
  }
}