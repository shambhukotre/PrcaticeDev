import { Component } from '@angular/core';
import { UserService } from '../../../services/user.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OnInit } from '@angular/core';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule,FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {

username: string = '';
password: string = '';

onclogin() {
  alert(`Username: ${this.username}, Password: ${this.password}`);
}

//Dipsaly users from api
  users: any[] = [];
constructor(private userService: UserService) {}

ngOnInit() {
  this.userService.getUsers().subscribe((data) => {
    this.users = data;
  });
}


//Get user by id
user: any;
userId!: number ;
getUserbyId(id: number) {
  this.userService.getUserById(id).subscribe((data) => {
    this.user = data;
    console.log(this.user);
  });

}
//Search user by name or email
searchQuery: string = '';
searchResults: any[] = [];
searchUsers() {
  this.userService.searchUsers(this.searchQuery).subscribe((data) => {
    this.searchResults = data;
    console.log(this.searchResults);
  });
}

// Add user form
newUser: any = {
  name: '',
  email: '',
  id : ''
}
createUser() {
  this.userService.createUser(this.newUser).subscribe((response) => {
    console.log('User created:', response); 
    this.users.push(response);
  }); 
}
}
