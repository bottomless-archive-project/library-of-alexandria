import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from "rxjs";
import {SearchHit} from "./domain/search-hit";
import {SearchResult} from "./domain/search-result";

@Injectable({
  providedIn: 'root'
})
export class SearchService {
  constructor(private http: HttpClient) {
  }

  getSearchStatistics(): Observable<any> {
    return this.http.get('/statistics');
  }

  searchDocuments(searchText: string, page: number, language: any, exactMatch: boolean, documentLength: any,
                  fileTypes: any): Observable<SearchResult> {
    let pageNumber: number = page * 10;
    let urlBase: string = '/document/find-by/keyword/' + searchText + '/?pageNumber=' + pageNumber;

    if (exactMatch) {
      urlBase += '&exactMatch=' + exactMatch;
    }

    if (language !== undefined) {
      urlBase += '&language=' + language.code;
    }

    if (documentLength !== undefined) {
      urlBase += '&documentLength=' + documentLength[0];
    }

    let types = Object.keys(fileTypes).filter(value => fileTypes[value]);
    if (types.length > 0) {
      urlBase += '&documentTypes=' + types.join();
    }

    return this.http.get<SearchResult>(urlBase);
  }
}
