import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {SearchResult} from './domain/search-result';

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
                  resultSize: number, fileTypes: Map<string, boolean>): Observable<SearchResult> {
    const pageNumber: number = page * 10;
    let urlBase: string = '/document/find-by/keyword/' + searchText
      + '/?pageNumber=' + pageNumber
      + '&resultSize=' + resultSize;

    if (exactMatch) {
      urlBase += '&exactMatch=' + exactMatch;
    }

    if (language !== undefined) {
      urlBase += '&language=' + language.code;
    }

    if (documentLength !== undefined) {
      urlBase += '&documentLength=' + documentLength[0];
    }


    const values: string[] = [];
    for (const [type, value] of fileTypes) {
      if (value) {
        values.push(type);
      }
    }

    if (values.length > 0) {
      urlBase += '&documentTypes=' + values.join();
    }

    return this.http.get<SearchResult>(urlBase);
  }
}
