import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {DebugDocument} from './domain/debug-document';
import {DebugLocation} from './domain/debug-location';

@Injectable({
  providedIn: 'root'
})
export class DebugService {

  constructor(private http: HttpClient) {
  }

  queryDocument(documentId: string): Observable<DebugDocument> {
    return this.http.get<DebugDocument>('/document/' + documentId + '/debug');
  }

  queryLocation(locationId: string): Observable<DebugLocation> {
    return this.http.get<DebugLocation>('/location/' + locationId + '/debug');
  }
}
