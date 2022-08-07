import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {UserInfo} from "./domain/user-info";
import {LoginResponse} from "./domain/login-response";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  public name: string;
  public isLoggedIn: boolean

  constructor(private http: HttpClient) {
  }

  updateUserInfo(): void {
    console.log("Querying site info!")

    this.http.get<UserInfo>('/user/info')
      .subscribe(result => {
        this.name = result.name;
        this.isLoggedIn = result.isLoggedIn;
      });
  }

  login(name: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/user/login', {
      username: name,
      password: password
    });
  }
}
