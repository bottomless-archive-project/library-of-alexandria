package com.github.loa.vault.domain;

public enum VaultType {

    /**
     * The vault location is directly on the disks.
     */
    FILE,

    /**
     * An AWS S3 compatible backend.
     *
     * @see <a href="https://aws.amazon.com/s3/features/?nc=sn&loc=2">https://aws.amazon.com/s3/features/?nc=sn&loc=2</a>
     */
    S3
}
