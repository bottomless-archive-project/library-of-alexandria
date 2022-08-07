import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {UserInfo} from "./domain/user-info";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) {
  }

  queryUserInfo(): Observable<UserInfo> {
    console.log("Querying site info!")

    return this.http.get<UserInfo>('/user/info');
  }
}
