import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './view/app/app.component';
import {HomeComponent} from './view/home/home.component';
import {SearchComponent} from './view/search/search.component';
import {DashboardStatisticsComponent} from './view/dashboard-statistics/dashboard-statistics.component';
import {DebugDocumentComponent} from './view/debug-document/debug-document.component';
import {HttpClientModule} from '@angular/common/http';
import {FormsModule} from '@angular/forms';
import {BytesPipe} from './bytes.pipe';
import {DashboardApplicationsComponent} from './view/dashboard-applications/dashboard-applications.component';
import { LoginComponent } from './view/login/login.component';
import {DebugLocationComponent} from './view/debug-location/debug-location.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    SearchComponent,
    DashboardStatisticsComponent,
    DashboardApplicationsComponent,
    DebugDocumentComponent,
    DebugLocationComponent,
    BytesPipe,
    LoginComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
