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

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title: string = 'loa-frontend';
  loading: boolean = false;
  usersEnabled: boolean = false;
  name: string;
  isLoggedIn: boolean;

  constructor(private router: Router, private siteInfoService: SiteInfoService, private userService: UserService) {
    siteInfoService.querySiteInfo().subscribe(siteInfo => {
      this.usersEnabled = siteInfo.usersEnabled;

      if (this.usersEnabled) {
        userService.updateUserInfo();

        this.name = userService.name;
        this.isLoggedIn = userService.isLoggedIn;
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
