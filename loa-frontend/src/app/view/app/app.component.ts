import {Component} from '@angular/core';
import {
  Event,
  NavigationCancel,
  NavigationEnd,
  NavigationError,
  NavigationStart,
  Router
} from '@angular/router';
import {SiteInfoService} from "../../shared/info/service/site-info.service";
import {UserService} from "../../shared/user/service/user-service";
import {UserInfo} from "../../shared/user/service/domain/user-info";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  loading: boolean = false;
  usersEnabled: boolean = false;
  userInfo: UserInfo;

  constructor(private router: Router, private siteInfoService: SiteInfoService, private userService: UserService) {
    siteInfoService.querySiteInfo()
      .subscribe(siteInfo => {
        this.usersEnabled = siteInfo.usersEnabled;

        if (this.usersEnabled) {
          userService.updateUserInfo();

          userService.userInfo
            .subscribe(userInfo => {
              console.log("Got new user info.", userInfo)

              this.userInfo = userInfo
            });
        }

        console.log("Users enabled: " + this.usersEnabled);
      });

    this.router.events.subscribe((event: Event) => {
      console.log('New route change event!', event);

      switch (true) {
        case event instanceof NavigationStart: {
          this.loading = true;
          break;
        }

        case event instanceof NavigationEnd:
        case event instanceof NavigationCancel:
        case event instanceof NavigationError: {
          this.loading = false;
          break;
        }
        default: {
          break;
        }
      }
    });
  }
}
