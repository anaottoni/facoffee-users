package facoffe.users.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import facoffe.users.model.User;

//Contem as informações de armazenamento de dados no BD
//Só acessa o banco
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    //Criar usuário
    
    //Busca user pelo e-mail
    User findByEmail(String email);
    //Verifica se já existe um user com esse e-mail
    Boolean existsByEmail(String email);

    

    //Listar os usuários cadastrados com filtros opcionais (status, role) e paginação.
    //Operação permitida somente para gestores

    ///Busca usuários filtrando pelo status
    Page<User> findByStatus(String status, Pageable pageable);

    //Busca usuários que possuem uma determinada role
    Page<User> findByRoles_Name(String role, Pageable pageable);

    //Busca usuários que possuem um status e uma role ao mesmo tempo
    Page<User> findByStatusAndRoles_Name(
            String status,
            String role,
            Pageable pageable
    );
}