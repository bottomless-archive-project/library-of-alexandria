export class DebugLocation {

  constructor(
    public id: string,
    public url: string,
    public source: string,
    public downloaderVersion: number,
    public downloadResultCode: string
  ) {
  }
}
