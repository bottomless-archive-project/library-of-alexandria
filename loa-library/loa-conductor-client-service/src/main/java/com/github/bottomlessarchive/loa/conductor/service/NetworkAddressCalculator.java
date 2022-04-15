package com.github.bottomlessarchive.loa.conductor.service;

import com.github.bottomlessarchive.loa.conductor.service.domain.NetworkAddressCalculationException;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

@Service
public class NetworkAddressCalculator {

    public InetAddress calculateInetAddress() {
        try {
            return NetworkInterface.networkInterfaces()
                            .flatMap(NetworkInterface::inetAddresses)
                            .filter(this::isUsableAddress)
                            .findFirst()
                            .orElse(InetAddress.getLocalHost());
        } catch (final SocketException | UnknownHostException e) {
            throw new NetworkAddressCalculationException("Unable to calculate network address for registration!", e);
        }
    }

    private boolean isUsableAddress(final InetAddress inetAddress) {
        return inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress();
    }
}
