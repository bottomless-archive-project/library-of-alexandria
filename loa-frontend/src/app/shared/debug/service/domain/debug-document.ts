export class DebugDocument {

  constructor(
    public id: string,
    public vault: string,
    public type: string,
    public status: string,
    public compression: string,
    public checksum: string,
    public fileSize: number,
    public downloadDate: string,
    public downloaderVersion: number,
    public inVault: boolean,
    public inIndex: boolean,
    public sourceLocations: string[]
  ) {
  }
}
