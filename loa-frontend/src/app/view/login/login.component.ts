import {Component} from '@angular/core';
import {UserService} from "../../shared/user/service/user-service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  username: string;
  password: string;

  invalidCredentials: boolean = false;

  constructor(private userService: UserService) {
  }

  login(): void {
    this.userService.login(this.username, this.password)
      .subscribe(loginResponse => {
        if (loginResponse.result === 'SUCCESSFUL') {
          this.userService.updateUserInfo();
        } else {
          this.invalidCredentials = true;
        }
      })
  }
}
