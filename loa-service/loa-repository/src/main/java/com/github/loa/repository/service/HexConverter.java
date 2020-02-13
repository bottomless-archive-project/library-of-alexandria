package com.github.loa.repository.service;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

@Service
public class HexConverter {

    public String encode(final byte[] target) {
        return Hex.encodeHexString(target);
    }

    public byte[] decode(final String target) {
        try {
            return Hex.decodeHex(target);
        } catch (final DecoderException e) {
            throw new RuntimeException("Unable to decode: " + target + "!", e);
        }
    }
}
