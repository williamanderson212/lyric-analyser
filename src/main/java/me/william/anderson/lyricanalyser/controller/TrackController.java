package me.william.anderson.lyricanalyser.controller;

import java.util.stream.Collectors;

import me.william.anderson.lyricanalyser.model.Track;
import me.william.anderson.lyricanalyser.repository.TrackRepository;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.val;

import static me.william.anderson.lyricanalyser.controller.Constants.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@SuppressWarnings("Duplicates")
@RestController
@RequestMapping(TRACKS_ROUTE)
public class TrackController {

    private final TrackRepository trackRepository;

    TrackController(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    @GetMapping
    public ResponseEntity<Resources<Resource<Track>>> findAll() {
        val tracks = trackRepository.findAll().stream()
                .map(track -> new Resource<>(track,
                        linkTo(methodOn(TrackController.class).findOne(track.getId())).withSelfRel(),
                        linkTo(methodOn(AlbumController.class).findOne(track.getAlbum().getId())).withRel(ALBUM_REL),
                        linkTo(methodOn(TrackController.class).findAll()).withRel(TRACKS_REL)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new Resources<>(tracks,
                        linkTo(methodOn(TrackController.class).findAll()).withSelfRel()));
    }

    @GetMapping(SEARCH_ROUTE)
    public ResponseEntity<Resources<Resource<Track>>> search(@RequestParam(NAME_PARAM) String name) {
        val tracks = trackRepository.findAllByNameIsLike(name).stream()
                .map(track -> new Resource<>(track,
                        linkTo(methodOn(TrackController.class).findOne(track.getId())).withSelfRel(),
                        linkTo(methodOn(AlbumController.class).findOne(track.getAlbum().getId())).withRel(ALBUM_REL),
                        linkTo(methodOn(TrackController.class).findAll()).withRel(TRACKS_REL)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new Resources<>(tracks,
                        linkTo(methodOn(TrackController.class).search(name)).withSelfRel()));
    }

    @GetMapping(FIND_ONE_ROUTE)
    public ResponseEntity<Resource<Track>> findOne(@PathVariable long id) {

        return trackRepository.findById(id)
                .map(track -> new Resource<>(track,
                        linkTo(methodOn(TrackController.class).findOne(track.getId())).withSelfRel(),
                        linkTo(methodOn(AlbumController.class).findOne(track.getAlbum().getId())).withRel(ALBUM_REL),
                        linkTo(methodOn(TrackController.class).findAll()).withRel(TRACKS_REL)))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(UPDATE_ROUTE)
    public ResponseEntity<Resource<Track>> markDuplicate(@PathVariable long id) {
        val track = trackRepository.getOne(id);

        if (track == null) {
            return ResponseEntity.notFound().build();
        }

        track.setDuplicate(!track.isDuplicate());
        trackRepository.save(track);

        return ResponseEntity.noContent().build();
    }
}
