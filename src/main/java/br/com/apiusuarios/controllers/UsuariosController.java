package br.com.apiusuarios.controllers;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.apiusuarios.dtos.AutenticarRequestDto;
import br.com.apiusuarios.dtos.AutenticarResponseDto;
import br.com.apiusuarios.dtos.CriarContaRequestDto;
import br.com.apiusuarios.dtos.CriarContaResponseDto;
import br.com.apiusuarios.dtos.RecuperarSenhaRequestDto;
import br.com.apiusuarios.dtos.RecuperarSenhaResponseDto;
import br.com.apiusuarios.entities.Usuario;
import br.com.apiusuarios.helpers.MD5Helper;
import br.com.apiusuarios.repositories.UsuarioRepository;
import br.com.apiusuarios.services.TokenService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/usuarios")
public class UsuariosController {

	@Autowired // inicialização automática
	private UsuarioRepository usuarioRepository;

	@Autowired //inicialização automática
	private TokenService tokenService;
	
	@PostMapping("autenticar")
	public ResponseEntity<AutenticarResponseDto> autenticar(@RequestBody @Valid AutenticarRequestDto dto) {

		try {
			AutenticarResponseDto response = new AutenticarResponseDto();
			
			//consultar o usuário no banco de dados através do email e da senha
			Usuario usuario = usuarioRepository.findByEmailAndSenha(dto.getEmail(), MD5Helper.encryptMD5(dto.getSenha()));
			
			//verificar se o usuário foi encontrado
			if(usuario != null) {
				
				response.setMensagem("Usuário autenticado com sucesso.");
				response.setIdUsuario(usuario.getIdUsuario());
				response.setNome(usuario.getNome());
				response.setEmail(usuario.getEmail());
				response.setAccesToken(tokenService.generateToken(usuario.getEmail()));
			
				//HTTP 200 (OK)
				return ResponseEntity.status(200).body(response);
			}
			else {
				response.setMensagem("Acesso negado. Usuário não encontrado.");
				return ResponseEntity.status(401).body(response);
			}
		}
		catch(Exception e) {
			//HTTP 500 (INTERNAL SERVER ERROR)
			return ResponseEntity.status(500).body(null);
		}
	}

	@PostMapping("criar-conta")
	public ResponseEntity<CriarContaResponseDto> criarConta(@RequestBody @Valid CriarContaRequestDto dto) {

		try {
			
			CriarContaResponseDto response = new CriarContaResponseDto();
			
			//verificar se já existe um usuário cadastrado com o email informado
			if(usuarioRepository.findByEmail(dto.getEmail()) != null) {
				
				response.setMensagem("O email informado já está cadastrado no sistema. Tente outro.");
				// HTTP STATUS 400 (BAD REQUEST)
				return ResponseEntity.status(400).body(response);
			}
			else {
				Usuario usuario = new Usuario();
				usuario.setNome(dto.getNome());
				usuario.setEmail(dto.getEmail());
				usuario.setSenha(MD5Helper.encryptMD5(dto.getSenha()));

				// gravando no banco de dados
				usuarioRepository.save(usuario);

				// criando os dados de resposta			
				response.setIdUsuario(usuario.getIdUsuario());
				response.setNome(usuario.getNome());
				response.setEmail(usuario.getEmail());
				response.setDataHoraCriacao(Instant.now());

				// HTTP STATUS 201 (Created)
				return ResponseEntity.status(201).body(response);
			}			
		} catch (Exception e) {
			// HTTP STATUS 500 (Internal Server Error)
			return ResponseEntity.status(500).body(null);
		}
	}

	@PostMapping("recuperar-senha")
	public ResponseEntity<RecuperarSenhaResponseDto> recuperarSenha(@RequestBody @Valid RecuperarSenhaRequestDto dto) {
		// TODO
		return null;
	}

}


