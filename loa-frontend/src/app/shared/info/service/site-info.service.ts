import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SiteInfo} from "./domain/site-info";

@Injectable({
  providedIn: 'root'
})
export class SiteInfoService {

  constructor(private http: HttpClient) {
  }

  querySiteInfo(): Observable<SiteInfo> {
    console.log("Querying site info!")

    return this.http.get<SiteInfo>('/info');
  }
}
