import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable, ReplaySubject} from "rxjs";
import {UserInfo} from "./domain/user-info";
import {LoginResponse} from "./domain/login-response";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  public userInfo: ReplaySubject<UserInfo> = new ReplaySubject<UserInfo>();

  constructor(private http: HttpClient) {
  }

  updateUserInfo(): void {
    console.log("Querying site info!")

    this.http.get<UserInfo>('/user/info')
      .subscribe(result => {
        this.userInfo.next(result);
      });
  }

  login(name: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/user/login', {
      username: name,
      password: password
    });
  }
}
