import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ApplicationInfoEntity} from "../domain/application-info-entity";

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {

  constructor(private http: HttpClient) {
  }

  getApplications(): Observable<ApplicationInfoEntity> {
    return this.http.get<ApplicationInfoEntity>('/dashboard/applications');
  }
}
