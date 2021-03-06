/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */
package org.restcomm.connect.dao.mybatis;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.restcomm.connect.dao.DaoUtils;
import org.restcomm.connect.dao.IncomingPhoneNumbersDao;
import org.restcomm.connect.dao.entities.IncomingPhoneNumberFilter;
import org.restcomm.connect.commons.dao.Sid;
import org.restcomm.connect.commons.annotations.concurrency.ThreadSafe;
import org.restcomm.connect.dao.entities.IncomingPhoneNumber;
import org.restcomm.connect.dao.entities.SearchFilterMode;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 * @author jean.deruelle@telestax.com
 * @author maria.farooq@telestax.com (Maria Farooq)
 */
@ThreadSafe
public final class MybatisIncomingPhoneNumbersDao implements IncomingPhoneNumbersDao {
    private static final String namespace = "org.mobicents.servlet.sip.restcomm.dao.IncomingPhoneNumbersDao.";
    private final SqlSessionFactory sessions;
    private final Logger logger = Logger.getLogger(MybatisIncomingPhoneNumbersDao.class.getName());
    private static final String ORG_SID = "organization_sid";
    private static final String PHONE_NUM = "phone_number";


    public MybatisIncomingPhoneNumbersDao(final SqlSessionFactory sessions) {
        super();
        this.sessions = sessions;
    }

    @Override
    public void addIncomingPhoneNumber(final IncomingPhoneNumber incomingPhoneNumber) {
        final SqlSession session = sessions.openSession();
        try {
            session.insert(namespace + "addIncomingPhoneNumber", toMap(incomingPhoneNumber));
            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public IncomingPhoneNumber getIncomingPhoneNumber(final Sid sid) {
        final SqlSession session = sessions.openSession();
        try {
            final Map<String, Object> result = session.selectOne(namespace + "getIncomingPhoneNumber", sid.toString());
            if (result != null) {
                return toIncomingPhoneNumber(result);
            } else {
                return null;
            }
        } finally {
            session.close();
        }
    }

    @Override
    public List<IncomingPhoneNumber> getIncomingPhoneNumbers(final Sid accountSid) {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getIncomingPhoneNumbers",
                    accountSid.toString());
            final List<IncomingPhoneNumber> incomingPhoneNumbers = new ArrayList<IncomingPhoneNumber>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    incomingPhoneNumbers.add(toIncomingPhoneNumber(result));
                }
            }
            return incomingPhoneNumbers;
        } finally {
            session.close();
        }
    }

    @Override
    public List<IncomingPhoneNumber> getIncomingPhoneNumbersRegex(IncomingPhoneNumberFilter incomingPhoneNumberFilter) {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getIncomingPhoneNumbersRegex", incomingPhoneNumberFilter);
            final List<IncomingPhoneNumber> incomingPhoneNumbers = new ArrayList<IncomingPhoneNumber>(results.size());
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    incomingPhoneNumbers.add(toIncomingPhoneNumber(result));
                }
            }
            return incomingPhoneNumbers;
        } finally {
            session.close();
        }
    }



    @Override
    public List<IncomingPhoneNumber> getIncomingPhoneNumbersByFilter(IncomingPhoneNumberFilter filter) {
        final SqlSession session = sessions.openSession();
        try {

            String query = "getIncomingPhoneNumbersByFriendlyName";
            if (filter.getFilterMode().equals(SearchFilterMode.WILDCARD_MATCH)) {
                query = "searchNumbersWithWildcardMode";
            }

            final List<Map<String, Object>> results = session.selectList(namespace + query,
                    filter);
            final List<IncomingPhoneNumber> incomingPhoneNumbers = new ArrayList<IncomingPhoneNumber>(results.size());
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    incomingPhoneNumbers.add(toIncomingPhoneNumber(result));
                }
            }
            return incomingPhoneNumbers;
        } finally {
            session.close();
        }
    }

    @Override
    public void removeIncomingPhoneNumber(final Sid sid) {
        removeIncomingPhoneNumbers("removeIncomingPhoneNumber", sid);
    }

    @Override
    public void removeIncomingPhoneNumbers(final Sid accountSid) {
        removeIncomingPhoneNumbers("removeIncomingPhoneNumbers", accountSid);
    }

    private void removeIncomingPhoneNumbers(final String selector, final Sid sid) {
        final SqlSession session = sessions.openSession();
        try {
            session.delete(namespace + selector, sid.toString());
            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateIncomingPhoneNumber(final IncomingPhoneNumber incomingPhoneNumber) {
        final SqlSession session = sessions.openSession();
        try {
            session.update(namespace + "updateIncomingPhoneNumber", toMap(incomingPhoneNumber));
            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public Integer getTotalIncomingPhoneNumbers(IncomingPhoneNumberFilter filter) {

        try (final SqlSession session = sessions.openSession();) {
            final Integer total = session.selectOne(namespace + "getTotalIncomingPhoneNumbersByUsingFilters", filter);
            return total;
        }
    }

    private IncomingPhoneNumber toIncomingPhoneNumber(final Map<String, Object> map) {
        final Sid sid = DaoUtils.readSid(map.get("sid"));
        final DateTime dateCreated = DaoUtils.readDateTime(map.get("date_created"));
        final DateTime dateUpdated = DaoUtils.readDateTime(map.get("date_updated"));
        final String friendlyName = DaoUtils.readString(map.get("friendly_name"));
        final Sid accountSid = DaoUtils.readSid(map.get("account_sid"));
        final String phoneNumber = DaoUtils.readString(map.get(PHONE_NUM));
        final String apiVersion = DaoUtils.readString(map.get("api_version"));
        final Boolean hasVoiceCallerIdLookup = DaoUtils.readBoolean(map.get("voice_caller_id_lookup"));
        final URI voiceUrl = DaoUtils.readUri(map.get("voice_url"));
        final String voiceMethod = DaoUtils.readString(map.get("voice_method"));
        final URI voiceFallbackUrl = DaoUtils.readUri(map.get("voice_fallback_url"));
        final String voiceFallbackMethod = DaoUtils.readString(map.get("voice_fallback_method"));
        final URI statusCallback = DaoUtils.readUri(map.get("status_callback"));
        final String statusCallbackMethod = DaoUtils.readString(map.get("status_callback_method"));
        final Sid voiceApplicationSid = DaoUtils.readSid(map.get("voice_application_sid"));
        final URI smsUrl = DaoUtils.readUri(map.get("sms_url"));
        final String smsMethod = DaoUtils.readString(map.get("sms_method"));
        final URI smsFallbackUrl = DaoUtils.readUri(map.get("sms_fallback_url"));
        final String smsFallbackMethod = DaoUtils.readString(map.get("sms_fallback_method"));
        final Sid smsApplicationSid = DaoUtils.readSid(map.get("sms_application_sid"));
        final URI uri = DaoUtils.readUri(map.get("uri"));
        final URI ussdUrl = DaoUtils.readUri(map.get("ussd_url"));
        final String ussdMethod = DaoUtils.readString(map.get("ussd_method"));
        final URI ussdFallbackUrl = DaoUtils.readUri(map.get("ussd_fallback_url"));
        final String ussdFallbackMethod = DaoUtils.readString(map.get("ussd_fallback_method"));
        final Sid ussdApplicationSid = DaoUtils.readSid(map.get("ussd_application_sid"));
        final URI referUrl = DaoUtils.readUri(map.get("refer_url"));
        final String referMethod = DaoUtils.readString(map.get("refer_method"));
        final Sid referApplicationSid = DaoUtils.readSid(map.get("refer_application_sid"));
        final Sid organizationSid = DaoUtils.readSid(map.get(ORG_SID));


        final Boolean voiceCapable = DaoUtils.readBoolean(map.get("voice_capable"));
        final Boolean smsCapable = DaoUtils.readBoolean(map.get("sms_capable"));
        final Boolean mmsCapable = DaoUtils.readBoolean(map.get("mms_capable"));
        final Boolean faxCapable = DaoUtils.readBoolean(map.get("fax_capable"));
        final Boolean pureSip = DaoUtils.readBoolean(map.get("pure_sip"));
        final String cost = DaoUtils.readString(map.get("cost"));
        // foreign properties loaded from applications table
        final String voiceApplicationName = DaoUtils.readString(map.get("voice_application_name"));
        final String smsApplicationName = DaoUtils.readString(map.get("sms_application_name"));
        final String ussdApplicationName = DaoUtils.readString(map.get("ussd_application_name"));
        final String referApplicationName = DaoUtils.readString(map.get("refer_application_name"));

        return new IncomingPhoneNumber(sid, dateCreated, dateUpdated, friendlyName, accountSid, phoneNumber, cost, apiVersion,
                hasVoiceCallerIdLookup, voiceUrl, voiceMethod, voiceFallbackUrl, voiceFallbackMethod, statusCallback,
                statusCallbackMethod, voiceApplicationSid, smsUrl, smsMethod, smsFallbackUrl, smsFallbackMethod,
                smsApplicationSid, uri, ussdUrl, ussdMethod, ussdFallbackUrl, ussdFallbackMethod, ussdApplicationSid,
                referUrl, referMethod, referApplicationSid,
                voiceCapable, smsCapable, mmsCapable, faxCapable, pureSip, voiceApplicationName, smsApplicationName, ussdApplicationName, referApplicationName, organizationSid);
    }

    private Map<String, Object> toMap(final IncomingPhoneNumber incomingPhoneNumber) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("sid", DaoUtils.writeSid(incomingPhoneNumber.getSid()));
        map.put("date_created", DaoUtils.writeDateTime(incomingPhoneNumber.getDateCreated()));
        map.put("date_updated", DaoUtils.writeDateTime(incomingPhoneNumber.getDateUpdated()));
        map.put("friendly_name", incomingPhoneNumber.getFriendlyName());
        map.put("account_sid", DaoUtils.writeSid(incomingPhoneNumber.getAccountSid()));
        map.put(PHONE_NUM, incomingPhoneNumber.getPhoneNumber());
        map.put("api_version", incomingPhoneNumber.getApiVersion());
        map.put("voice_caller_id_lookup", incomingPhoneNumber.hasVoiceCallerIdLookup());
        map.put("voice_url", DaoUtils.writeUri(incomingPhoneNumber.getVoiceUrl()));
        map.put("voice_method", incomingPhoneNumber.getVoiceMethod());
        map.put("voice_fallback_url", DaoUtils.writeUri(incomingPhoneNumber.getVoiceFallbackUrl()));
        map.put("voice_fallback_method", incomingPhoneNumber.getVoiceFallbackMethod());
        map.put("status_callback", DaoUtils.writeUri(incomingPhoneNumber.getStatusCallback()));
        map.put("status_callback_method", incomingPhoneNumber.getStatusCallbackMethod());
        map.put("voice_application_sid", DaoUtils.writeSid(incomingPhoneNumber.getVoiceApplicationSid()));
        map.put("sms_url", DaoUtils.writeUri(incomingPhoneNumber.getSmsUrl()));
        map.put("sms_method", incomingPhoneNumber.getSmsMethod());
        map.put("sms_fallback_url", DaoUtils.writeUri(incomingPhoneNumber.getSmsFallbackUrl()));
        map.put("sms_fallback_method", incomingPhoneNumber.getSmsFallbackMethod());
        map.put("sms_application_sid", DaoUtils.writeSid(incomingPhoneNumber.getSmsApplicationSid()));
        map.put("uri", DaoUtils.writeUri(incomingPhoneNumber.getUri()));
        map.put("ussd_url", DaoUtils.writeUri(incomingPhoneNumber.getUssdUrl()));
        map.put("ussd_method", incomingPhoneNumber.getUssdMethod());
        map.put("ussd_fallback_url", DaoUtils.writeUri(incomingPhoneNumber.getUssdFallbackUrl()));
        map.put("ussd_fallback_method", incomingPhoneNumber.getUssdFallbackMethod());
        map.put("ussd_application_sid", DaoUtils.writeSid(incomingPhoneNumber.getUssdApplicationSid()));
        map.put("refer_url", DaoUtils.writeUri(incomingPhoneNumber.getReferUrl()));
        map.put("refer_method", incomingPhoneNumber.getReferMethod());
        map.put("refer_application_sid", DaoUtils.writeSid(incomingPhoneNumber.getReferApplicationSid()));
        map.put("voice_capable", incomingPhoneNumber.isVoiceCapable());
        map.put("sms_capable", incomingPhoneNumber.isSmsCapable());
        map.put("mms_capable", incomingPhoneNumber.isMmsCapable());
        map.put("fax_capable", incomingPhoneNumber.isFaxCapable());
        map.put("pure_sip", incomingPhoneNumber.isPureSip());
        map.put("cost", incomingPhoneNumber.getCost());
        map.put(ORG_SID, DaoUtils.writeSid(incomingPhoneNumber.getOrganizationSid()));
        return map;
    }

}
