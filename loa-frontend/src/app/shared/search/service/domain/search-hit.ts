export class SearchHit {

  constructor(
    public id: string,
    public title: string,
    public author: string,
    public description: string[],
    public type: string,
    public language: string,
    public pageCount: number,
    public vault: string,
    public source: string,
    public downloadDate: string,
    public sourceLocations: string[]
  ) {
  }
}
