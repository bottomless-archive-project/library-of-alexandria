package com.github.loa.vault.domain;

public enum VaultType {

    /**
     * The vault location is directly on the disks.
     */
    FILE,
    /**
     * The vault location is somewhere on the network communicating over SMB (Server Message Block).
     */
    SMB
}
