import {SearchHit} from "./search-hit";

export class SearchResult {

  constructor(
    public searchHits: SearchHit[],
    public totalHitCount: number
  ) {
  }
}
