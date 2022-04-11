import {Injectable} from '@angular/core';
import {Resolve} from '@angular/router';
import {Observable} from 'rxjs';
import {ApplicationService} from "../service/application.service";
import {ApplicationInfoEntity} from "../domain/application-info-entity";

@Injectable({
  providedIn: 'root'
})
export class ApplicationResolver implements Resolve<any> {

  constructor(private applicationService: ApplicationService) {
  }

  resolve(): Observable<ApplicationInfoEntity> {
    return this.applicationService.getApplications();
  }
}
