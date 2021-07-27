import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './view/app/app.component';
import {HomeComponent} from './view/home/home.component';
import {SearchComponent} from './view/search/search.component';
import {DashboardComponent} from './view/dashboard/dashboard.component';
import {DebugDocumentComponent} from './view/debug-document/debug-document.component';
import {HttpClientModule} from '@angular/common/http';
import {FormsModule} from '@angular/forms';
import {BytesPipe} from './bytes.pipe';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    SearchComponent,
    DashboardComponent,
    DebugDocumentComponent,
    BytesPipe
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
