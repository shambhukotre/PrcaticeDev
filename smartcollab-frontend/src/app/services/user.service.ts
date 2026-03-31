import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  users = [ 'John Doe', 'Jane Smith', 'Alice Johnson' , 'Bob Brown', 'Charlie Davis', 'Emily Wilson', 'Frank Miller', 'Grace Lee', 'Henry Clark', 'Isabella Turner' ];
  
//Calling api
private apiUrl = 'http://localhost:8080/api/users';


  constructor(private httpClient: HttpClient) { }

  getUsers():Observable<any> {
    return this.httpClient.get(this.apiUrl);
  }

  getUserById(id: number): Observable<any> {
    return this.httpClient.get(`${this.apiUrl}/${id}`);
  }

  searchUsers(name: string): Observable<any> {
    return this.httpClient.get(`${this.apiUrl}/search?name=${name}`);
  }

createUser(user: any): Observable<any> {
  return this.httpClient.post(this.apiUrl, user);
}

}
