package com.fsck.k9.mail.store.imap;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fsck.k9.mail.store.imap.ImapResponseParser.equalsIgnoreCase;
import static com.fsck.k9.mail.store.imap.ImapUtility.getImapSequenceValues;


class UidCopyResponse extends SelectedStateResponse {
    private Map<String, String> uidMapping;

    private UidCopyResponse(List<ImapResponse> imapResponse) {
        super(imapResponse);
    }

    public static UidCopyResponse parse(List<ImapResponse> imapResponses) {
        UidCopyResponse response = new UidCopyResponse(imapResponses);
        return response.uidMapping == null ? null : response;
    }

    @Override
    void parseResponse(List<ImapResponse> imapResponses) {
        ImapResponse response = ImapUtility.getLastResponse(imapResponses);

        if (!response.isTagged() || response.size() < 2 || !equalsIgnoreCase(response.get(0), Responses.OK) ||
                !response.isList(1)) {
            return;
        }

        ImapList responseTextList = response.getList(1);
        if (responseTextList.size() < 4 || !equalsIgnoreCase(responseTextList.get(0), Responses.COPYUID) ||
                !responseTextList.isString(1) || !responseTextList.isString(2) || !responseTextList.isString(3)) {
            return;
        }

        List<String> sourceUids = getImapSequenceValues(responseTextList.getString(2));
        List<String> destinationUids = getImapSequenceValues(responseTextList.getString(3));

        int size = sourceUids.size();
        if (size == 0 || size != destinationUids.size()) {
            return;
        }

        uidMapping = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            String sourceUid = sourceUids.get(i);
            String destinationUid = destinationUids.get(i);
            uidMapping.put(sourceUid, destinationUid);
        }
    }

    @Override
    void combine(SelectedStateResponse selectedStateResponse) {
        if (selectedStateResponse == null) {
            return;
        }
        UidCopyResponse copyUidResponse = (UidCopyResponse) selectedStateResponse;
        this.uidMapping.putAll(copyUidResponse.getUidMapping());
    }

    public Map<String, String> getUidMapping() {
        return uidMapping;
    }
}