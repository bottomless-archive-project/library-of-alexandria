import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Subject} from "rxjs";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})
export class SearchComponent implements OnInit {

  statistics: any;
  searchText: string = '';
  modelChanged: Subject<string> = new Subject<string>();
  documentLengths: string[][];
  documentLength: any = undefined;
  languages: string[][];
  language: any = undefined;
  fileTypes: any = {
    PDF: false,
    DOC: false,
    PPT: false,
    RTF: false,
    XLS: false,
    DOCX: false,
    XLSX: false,
    EPUB: false,
    MOBI: false
  };
  hits: any[] = [];
  hitCount = 0;
  totalPages = 0;
  page = 0;
  loading = false;

  constructor(private route: ActivatedRoute, private http: HttpClient) {
    this.modelChanged.pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(searchText => {
        console.log(searchText);

        return this.searchText = searchText;
      });

    this.languages = [
      ['af', 'afrikaans'],
      ['sq', 'albanian'],
      ['ar', 'arabic'],
      ['hy', 'armenian'],
      ['az', 'azerbaijani'],
      ['eu', 'basque'],
      ['be', 'belarusian'],
      ['bn', 'bengali'],
      ['nb', 'bokmal'],
      ['bs', 'bosnian'],
      ['bg', 'bulgarian'],
      ['ca', 'catalan'],
      ['zh', 'chinese'],
      ['hr', 'croatian'],
      ['cs', 'czech'],
      ['da', 'danish'],
      ['nl', 'dutch'],
      ['en', 'english'],
      ['eo', 'esperanto'],
      ['et', 'estonian'],
      ['fi', 'finnish'],
      ['fr', 'french'],
      ['lg', 'ganda'],
      ['ka', 'georgian'],
      ['de', 'german'],
      ['el', 'greek'],
      ['gu', 'gujarati'],
      ['he', 'hebrew'],
      ['hi', 'hindi'],
      ['hu', 'hungarian'],
      ['is', 'icelandic'],
      ['id', 'indonesian'],
      ['ga', 'irish'],
      ['it', 'italian'],
      ['ja', 'japanese'],
      ['kk', 'kazakh'],
      ['ko', 'korean'],
      ['la', 'latin'],
      ['lv', 'latvian'],
      ['lt', 'lithuanian'],
      ['mk', 'macedonian'],
      ['ms', 'malay'],
      ['mr', 'marathi'],
      ['mn', 'mongolian'],
      ['nn', 'nynorsk'],
      ['fa', 'persian'],
      ['pl', 'polish'],
      ['pt', 'portuguese'],
      ['pa', 'punjabi'],
      ['ro', 'romanian'],
      ['ru', 'russian'],
      ['sr', 'serbian'],
      ['sn', 'shona'],
      ['sk', 'slovak'],
      ['sl', 'slovene'],
      ['so', 'somali'],
      ['st', 'sotho'],
      ['es', 'spanish'],
      ['sw', 'swahili'],
      ['sv', 'swedish'],
      ['tl', 'tagalog'],
      ['ta', 'tamil'],
      ['te', 'telugu'],
      ['th', 'thai'],
      ['ts', 'tsonga'],
      ['tn', 'tswana'],
      ['tr', 'turkish'],
      ['uk', 'ukrainian'],
      ['ur', 'urdu'],
      ['vi', 'vietnamese'],
      ['cy', 'welsh'],
      ['xh', 'xhosa'],
      ['yo', 'yoruba'],
      ['zu', 'zulu']
    ]

    this.documentLengths = [
      ['SHORT_STORY', 'Short story (1 - 10 pages)'],
      ['NOVELETTE', 'Novelette (11 - 50 pages)'],
      ['NOVELLA', 'Novella (51 - 150 pages)'],
      ['NOVEL', 'Novel (150+ pages)']
    ];
  }

  ngOnInit(): void {
    this.statistics = this.route.snapshot.data.statistics;
  }

  changed(text: string) {
    this.modelChanged.next(text);
  }

  setLanguage(language: any) {
    this.language = language;

    this.refreshHits();
  };

  setDocumentLength(documentLength: any) {
    this.documentLength = documentLength;

    this.refreshHits();
  };

  refreshHits() {
    console.log("Should refresh hits here!", this.fileTypes);

    if (this.searchText === "") {
      this.hits = [];
      this.hitCount = 0;
      this.totalPages = 0;

      return;
    }

    this.loading = true;

    var pageNumber = this.page * 10;
    var exactMatch = this.searchText.startsWith("\"") && this.searchText.endsWith("\"");

    var urlBase = '/document/find-by/keyword/' + this.searchText + '/?pageNumber=' + pageNumber;

    if (exactMatch) {
      urlBase += '&exactMatch=' + exactMatch;
    }

    if (this.language !== undefined) {
      urlBase += '&language=' + this.language.code;
    }

    if (this.documentLength !== undefined) {
      urlBase += '&documentLength=' + this.documentLength[0];
    }

    var types = Object.keys(this.fileTypes).filter(value => this.fileTypes[value]);
    if (types.length > 0) {
      urlBase += '&documentTypes=' + types.join();
    }

    //TODO: Use a service!
    this.http.get(urlBase)
      .subscribe(response => {
        this.hits = response.searchHits;
        this.hitCount = response.totalHitCount;
        this.totalPages = Math.ceil(response.totalHitCount / 10);
        this.loading = false;
      });
  }
}
