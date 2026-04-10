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
loading: boolean = false;
loadingCreate: boolean = false;
loadingDelete: boolean = false;

onclogin() {
  alert(`Username: ${this.username}, Password: ${this.password}`);
}

//Dipsaly users from api
  users: any[] = [];
constructor(private userService: UserService) {}

ngOnInit() {
  this.userService.getUsers().subscribe((data) => {
    this.users = data;
    error: (error: any) => {
      console.error('Error fetching users:', error);
    } 
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
}
createUser() {
  this.loadingCreate = true;
  this.userService.createUser(this.newUser).subscribe({
    next: (response) => {
      console.log('User created:', response); 
      this.users.push(response);
      this.loadingCreate = false;
      this.newUser = { name: '', email: '', id: '' };
    },
    error: (error) => {
      console.error('Error creating user:', error);
      this.loadingCreate = false;
    }
  }); 
}
// Update user form with user id
updatedUser: any = {
  name: '',
  email: '' 
}

updateUser() {
  if (this.userId) {
    // Include id in the request body
    const userWithId = { ...this.updatedUser, id: this.userId };
    this.loading = true;
    this.userService.updateUser(this.userId, userWithId).subscribe({
      next: (response) => {
        console.log('User updated:', response);
        const index = this.users.findIndex((u) => u.id === this.userId);
        if (index !== -1) {
          this.users[index] = response;
        } else {    
          this.users.push(response);
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Full error response:', error);
        console.error('Backend message:', error.error?.message);
        console.error('Backend trace:', error.error?.trace);
        this.loading = false;
      }
    });
  } else {
    console.error('User ID is required for update.');
  } 
}

//delete user by id
deleteUserId!: number ;
deleteUser(id: number) {
  this.loadingDelete = true;
  this.userService.deleteUser(id).subscribe({
    next: () => { 
      console.log('User deleted successfully');
      this.users = this.users.filter((u) => u.id !== id);
      this.loadingDelete = false;
      this.deleteUserId = null as any;
    },
    error: (error) => {
      console.error('Error deleting user:', error); 
      this.loadingDelete = false;
    }
  });
}


}
