import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from "rxjs";

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
                  fileTypes: any): Observable<any> {
    var pageNumber = page * 10;
    var urlBase = '/document/find-by/keyword/' + searchText + '/?pageNumber=' + pageNumber;

    if (exactMatch) {
      urlBase += '&exactMatch=' + exactMatch;
    }

    if (language !== undefined) {
      urlBase += '&language=' + language.code;
    }

    if (documentLength !== undefined) {
      urlBase += '&documentLength=' + documentLength[0];
    }

    var types = Object.keys(fileTypes).filter(value => fileTypes[value]);
    if (types.length > 0) {
      urlBase += '&documentTypes=' + types.join();
    }

    return this.http.get(urlBase);
  }
}
