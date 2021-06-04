import {Injectable} from '@angular/core';
import {Resolve} from '@angular/router';
import {Observable} from 'rxjs';

import {SearchService} from '../service/search.service';

@Injectable({
  providedIn: 'root'
})
export class SearchResolver implements Resolve<any> {
  constructor(private searchService: SearchService) {
  }

  resolve(): Observable<any> {
    return this.searchService.getSearchStatistics();
  }
}
