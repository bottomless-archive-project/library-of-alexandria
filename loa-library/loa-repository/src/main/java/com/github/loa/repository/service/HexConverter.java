package com.github.loa.repository.service;

import com.github.loa.repository.service.domain.HexConversionException;
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
            throw new HexConversionException("Unable to decode: " + target + "!", e);
        }
    }
}
